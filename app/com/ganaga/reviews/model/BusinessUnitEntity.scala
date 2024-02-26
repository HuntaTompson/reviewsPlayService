package com.ganaga.reviews.model
import play.api.libs.json.Json

object BusinessUnitEntity {
  implicit val businessUnitEntityFormat = Json.format[BusinessUnitEntity]
}

case class BusinessUnitEntity(businessUnitId: String,
                              identifyingName: String,
                              displayName: String,
                              latestReviewCount: Option[Int],
                              totalNumberOfReviews: Int,
                              latestReview: Review)


