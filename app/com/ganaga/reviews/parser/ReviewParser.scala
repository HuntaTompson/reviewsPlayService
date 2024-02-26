package com.ganaga.reviews.parser

import com.ganaga.reviews.model.Review
import play.api.libs.json.JsArray
import play.api.libs.json.Json

case class ReviewParser() {
  def parseFirstReview(jsonString: String): Review = {
    import Review._
    val firstReviewJsValue = (Json.parse(jsonString)\"reviews").as[JsArray].value.head
    firstReviewJsValue.as[Review]
  }
}
