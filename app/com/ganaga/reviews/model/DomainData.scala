package com.ganaga.reviews.model

import play.api.libs.json.Json

object DomainData {
  implicit val domainDataFormat = Json.format[DomainData]
}
case class DomainData(domainName: String, latestReview: Review, latestReviewCount: Int, totalReviewCount: Int, traffic: Long)
