package com.ganaga.reviews.services

import akka.Done
import akka.NotUsed
import akka.stream.Materializer
import akka.stream.scaladsl.Flow
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source
import com.ganaga.reviews.Utils._
import com.ganaga.reviews.model.BusinessUnitEntity
import com.ganaga.reviews.model.BusinessUnitParserModel
import com.ganaga.reviews.model.Review
import com.ganaga.reviews.parser.BusinessUnitAkkaParser
import com.ganaga.reviews.parser.BusinessUnitAkkaParser.businessUnitsPathFormat
import com.ganaga.reviews.services.BusinessUnitService._
import com.ganaga.reviews.store.BusinessUnitsStore
import com.ganaga.reviews.store.Categories
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.libs.ws.WSClient

import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

object BusinessUnitAkkaService {
  val businessUnitsPathFormat = "https://www.trustpilot.com/categories/%s?sort=latest_review"
}

case class BusinessUnitAkkaService @Inject() (buParser: BusinessUnitAkkaParser, reviewService: ReviewService)
                                  (implicit executionContext: ExecutionContext, materializer: Materializer) {

  val parseRecentlyReviewedFlow: Flow[String, BusinessUnitParserModel, NotUsed] =
    Flow[String]
      .mapAsync(4)(getDocumentF)
      .mapAsync(4)(buParser.parseDocument)
      .mapConcat(identity)
//  val parseRecentlyReviewedFlow = buParser.parseRecentlyReviewedFlow

  val parsedModelToEntityFlow =
    Flow[BusinessUnitParserModel]
    .mapAsync(4)(bu => parsedModelToEntity(bu))
    .filter(isReviewDateValid)

  val mergeWithStoredDataSink = Sink.foreach[BusinessUnitEntity](mergeWithStoredData)

  def updateRecentlyReviewedF(): Future[Done] = {
    Categories.categoriesSource
      .via(parseRecentlyReviewedFlow)
      .via(parsedModelToEntityFlow)
      .runWith(mergeWithStoredDataSink)
  }

  def parsedModelToEntity(parseModel: BusinessUnitParserModel): Future[BusinessUnitEntity] = {
    reviewService.fetchLatestReview(parseModel.businessUnitId)
      .map(review => toBusinessUnitEntity(parseModel, review))
  }

  def mergeWithStoredData(buEntity: BusinessUnitEntity): Unit = {
    val storedData = BusinessUnitsStore.getAllBusinessUnits()
    storedData.get(buEntity.businessUnitId) match {
      case Some(stored) if isReviewNotSame(stored, buEntity) =>
        BusinessUnitsStore.saveBusinessUnit(buEntity.copy(latestReviewCount = stored.latestReviewCount.map(_ + 1)))
      case Some(stored) =>
        BusinessUnitsStore.saveBusinessUnit(stored.copy(totalNumberOfReviews = buEntity.totalNumberOfReviews))
      case None =>
        BusinessUnitsStore.saveBusinessUnit(buEntity.copy(latestReviewCount = Some(1)))
    }
  }

  private def toBusinessUnitEntity(parserModel: BusinessUnitParserModel, review: Review): BusinessUnitEntity = {
    BusinessUnitEntity(parserModel.businessUnitId,
      parserModel.identifyingName,
      parserModel.displayName,
      None,
      parserModel.numberOfReviews,
      review)
  }

  def getDocumentF(categoryId: String): Future[Document] = Future{
    val url = businessUnitsPathFormat.format(categoryId)
    Jsoup.connect(url).get()
  }
}
