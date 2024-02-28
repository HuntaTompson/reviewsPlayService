package com.ganaga.reviews.services

import akka.Done
import akka.NotUsed
import akka.stream.Materializer
import akka.stream.scaladsl.Flow
import akka.stream.scaladsl.Sink
import com.ganaga.reviews.Utils._
import com.ganaga.reviews.model.BusinessUnitEntity
import com.ganaga.reviews.model.BusinessUnitParserModel
import com.ganaga.reviews.model.Review
import com.ganaga.reviews.parser.BusinessUnitParser
import com.ganaga.reviews.store.BusinessUnitsStore
import com.ganaga.reviews.store.CategoriesStore

import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

object BusinessUnitProcessService {
  val businessUnitsPathFormat = "https://www.trustpilot.com/categories/%s?sort=latest_review"
}

case class BusinessUnitProcessService @Inject()(buParser: BusinessUnitParser, reviewService: ReviewService)
                                               (implicit executionContext: ExecutionContext, materializer: Materializer) {

  val parseRecentlyReviewedFlow: Flow[String, BusinessUnitParserModel, NotUsed] = buParser.parseRecentlyReviewedFlow

  val parsedModelToEntityFlow: Flow[BusinessUnitParserModel, BusinessUnitEntity, NotUsed] =
    Flow[BusinessUnitParserModel]
    .mapAsyncUnordered(4)(bu => parsedModelToEntity(bu))
    .filter(isReviewDateValid)

  val mergeWithStoredDataSink: Sink[BusinessUnitEntity, Future[Done]] = Sink.foreach[BusinessUnitEntity](mergeWithStoredData)

  def updateRecentlyReviewed(): Future[Done] = {
    CategoriesStore.categoriesSource
      .via(parseRecentlyReviewedFlow)
      .via(parsedModelToEntityFlow)
      .runWith(mergeWithStoredDataSink)
  }

  private def parsedModelToEntity(parseModel: BusinessUnitParserModel): Future[BusinessUnitEntity] = {
    reviewService.fetchLatestReview(parseModel.businessUnitId)
      .map(review => toBusinessUnitEntity(parseModel, review))
  }

  private def mergeWithStoredData(buEntity: BusinessUnitEntity): Unit = {
    BusinessUnitsStore.getBusinessUnit(buEntity.businessUnitId) match {
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
