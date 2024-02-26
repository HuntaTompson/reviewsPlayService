package com.ganaga.reviews.services

import akka.stream.scaladsl.Source
import com.ganaga.reviews.Utils._
import com.ganaga.reviews.model.BusinessUnitEntity
import com.ganaga.reviews.model.BusinessUnitParserModel
import com.ganaga.reviews.model.Review
import com.ganaga.reviews.parser.BusinessUnitParser
import com.ganaga.reviews.parser.ReviewParser
import com.ganaga.reviews.services.BusinessUnitService._
import com.ganaga.reviews.store.BusinessUnitsStore
import com.ganaga.reviews.store.Categories
import play.api.libs.ws.WSClient

import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

object BusinessUnitService {

  val reviewUrlFormat = s"https://www.trustpilot.com/api/categoriespages/%s/reviews?locale=en-US"
}

case class BusinessUnitService @Inject()(wsClient: WSClient, buParser: BusinessUnitParser, reviewParser: ReviewParser)(implicit executionContext: ExecutionContext) {

  def updateRecentlyReviewed(): Future[Unit] = {
    val l1 = System.currentTimeMillis()
    val recentlyReviewed = buParser.parseRecentlyReviewed(Categories.categoriesIds)
    val l2 = System.currentTimeMillis()
    println(s"recentlyReviewed finished. Time - ${l2 - l1}")
    val l3 = System.currentTimeMillis()
    val withLatestReview = parsedModelToEntity(recentlyReviewed)
    withLatestReview.map{bus =>
      val l4 = System.currentTimeMillis()
      println(s"withLatestReview finished. Time - ${l4 - l3}")
      mergeWithStoredData(bus)
    }
  }

  def parsedModelToEntity(parseModels: List[BusinessUnitParserModel]): Future[List[BusinessUnitEntity]] = {
    Future.sequence(parseModels.map(bu => parsedModelToEntity(bu)))
  }

  def parsedModelToEntity(parseModel: BusinessUnitParserModel): Future[BusinessUnitEntity] = {
    wsClient.url(reviewUrlFormat.format(parseModel.businessUnitId)).get()
      .map(resp => toBusinessUnitEntity(
          parseModel,
          reviewParser.parseFirstReview(resp.body))
      )
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
