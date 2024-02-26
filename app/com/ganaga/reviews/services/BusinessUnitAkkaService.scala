package com.ganaga.reviews.services

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
import com.ganaga.reviews.parser.BusinessUnitParser
import com.ganaga.reviews.parser.ReviewParser
import com.ganaga.reviews.services.BusinessUnitService._
import com.ganaga.reviews.store.BusinessUnitsStore
import com.ganaga.reviews.store.Categories
import play.api.libs.ws.WSClient
import play.api.libs.ws.WSRequest

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

object BusinessUnitAkkaService {
  val reviewUrlFormat = s"https://www.trustpilot.com/api/categoriespages/%s/reviews?locale=en-US"
}

case class BusinessUnitAkkaService(wsClient: WSClient, buParser: BusinessUnitAkkaParser, reviewService: ReviewService)
                                  (implicit executionContext: ExecutionContext, materializer: Materializer) {

  val parsedModelToEntityFlow = Flow[BusinessUnitParserModel].mapAsync(4)(bu => parsedModelToEntity(bu))
  val parseRecenlyReviewedFlow = buParser.parseRecentlyReviewedFlow

  def updateRecentlyReviewed(): Future[Unit] = {
    val l1 = System.currentTimeMillis()
    val recentlyReviewed = buParser.parseRecentlyReviewed(Categories.categoriesIds)
    val l2 = System.currentTimeMillis()
    println(s"recentlyReviewed finished. Time - ${l2 - l1}")
    val l3 = System.currentTimeMillis()
    val withLatestReview = parsedModelToEntityS(recentlyReviewed)
    withLatestReview.map{bus =>
      val l4 = System.currentTimeMillis()
      println(s"withLatestReview finished. Time - ${l4 - l3}")
      mergeWithStoredData(bus)
    }
  }

//  def parsedModelToEntity(parseModels: List[BusinessUnitParserModel]): Future[List[BusinessUnitEntity]] = {
//    Future.sequence(parseModels.map(bu => parsedModelToEntity(bu)))
//  }

  def parsedModelToEntityS(parseModels: List[BusinessUnitParserModel]): Future[List[BusinessUnitEntity]] = {
    Source(parseModels)
      .mapAsyncUnordered(4)(bu => parsedModelToEntity(bu))
      .runWith(Sink.collection[BusinessUnitEntity, List[BusinessUnitEntity]])
//    Future.sequence(parseModels.map(bu => parsedModelToEntity(bu)))
  }

  def parsedModelToEntityF(): Flow[BusinessUnitParserModel, BusinessUnitEntity, NotUsed] = {
    Flow[BusinessUnitParserModel].mapAsync(4)(bu => parsedModelToEntity(bu))
  }

  def parsedModelToEntity(parseModel: BusinessUnitParserModel): Future[BusinessUnitEntity] = {
    fetchLatestReview(parseModel.businessUnitId)
      .map(review => toBusinessUnitEntity(parseModel, review))
  }

  def fetchLatestReview(businessUnitId: String): Future[Review] = {
    wsClient.url(reviewUrlFormat.format(businessUnitId)).get().map(resp => reviewService.fetchFirstReview(resp.body))
  }

  def mergeWithStoredData(businessUnitEntities: List[BusinessUnitEntity]): Unit = {
    businessUnitEntities.filter(bu => isReviewDateValid(bu)).foreach(bu => mergeWithStoredData(bu))
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
}
