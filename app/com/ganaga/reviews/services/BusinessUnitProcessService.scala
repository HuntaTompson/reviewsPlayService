package com.ganaga.reviews.services

import akka.Done
import akka.NotUsed
import akka.stream.Materializer
import akka.stream.scaladsl.Flow
import akka.stream.scaladsl.Sink
import com.ganaga.reviews.Utils._
import com.ganaga.reviews.model.BusinessUnitEntity
import com.ganaga.reviews.model.BusinessUnitParserModel
import com.ganaga.reviews.model.BusinessUnitProcessData
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

  val parsedModelToDataFlow: Flow[BusinessUnitParserModel, BusinessUnitProcessData, NotUsed] =
    Flow[BusinessUnitParserModel]
      .mapAsyncUnordered(4)(bu => parsedModelToData(bu))
      .filter(data => data.reviews.exists(isReviewDateValid))

  val mergeWithStoredDataSink: Sink[BusinessUnitProcessData, Future[Done]] = Sink.foreach[BusinessUnitProcessData](mergeWithStoredData)

  def updateRecentlyReviewed(): Future[Done] = {
    CategoriesStore.categoriesSource
      .via(parseRecentlyReviewedFlow)
      .via(parsedModelToDataFlow)
      .runWith(mergeWithStoredDataSink)
  }

  private def parsedModelToData(parseModel: BusinessUnitParserModel): Future[BusinessUnitProcessData] = {
    reviewService.fetchLatestReviews(parseModel.businessUnitId)
      .map(reviews => BusinessUnitProcessData(parseModel, reviews))
  }

  private def mergeWithStoredData(data: BusinessUnitProcessData): Unit = {
    val buModel = data.buModel
    val stored = BusinessUnitsStore.getBusinessUnit(buModel.businessUnitId)
    val newReviews = filterNewReviews(stored, data.reviews)

    (stored, newReviews.nonEmpty) match {
      case (Some(entity), true) =>
        BusinessUnitsStore.saveBusinessUnit(entity.copy(
          latestReview = newReviews.head,
          latestReviewCount = entity.latestReviewCount.map(_ + newReviews.size)))
      case (Some(entity), false) =>
        BusinessUnitsStore.saveBusinessUnit(entity.copy(totalNumberOfReviews = buModel.numberOfReviews))
      case (None, true) =>
        BusinessUnitsStore.saveBusinessUnit(
          toBusinessUnitEntity(buModel, newReviews.head)
            .copy(latestReviewCount = Option(newReviews.size))
        )
      case (None, false) =>
      //just ignore this case
    }
  }

  def filterNewReviews(buEntity: Option[BusinessUnitEntity], reviews: List[Review]) = {
    val storedReview = buEntity.map(_.latestReview)
    val newReviews = reviews.filter(r => isReviewDateValid(r) && storedReview.forall(r.isCreatedAfter))
    newReviews
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
