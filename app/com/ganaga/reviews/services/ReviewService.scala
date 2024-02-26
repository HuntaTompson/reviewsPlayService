package com.ganaga.reviews.services

import com.ganaga.reviews.model.Review
import com.ganaga.reviews.services.ReviewService.reviewUrlFormat
import play.api.libs.json.JsArray
import play.api.libs.json.Json
import play.api.libs.ws.WSClient

import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

object ReviewService {
  val reviewUrlFormat = s"https://www.trustpilot.com/api/categoriespages/%s/reviews?locale=en-US"
}

class ReviewService @Inject()(wsClient: WSClient)(implicit executionContext: ExecutionContext) {

  private def getFirstReview(reviewsResp: String): Review = {
    import Review._
    val firstReviewJsValue = (Json.parse(reviewsResp)\"reviews").as[JsArray].value.head
    firstReviewJsValue.as[Review]
  }

  def fetchLatestReview(businessUnitId: String): Future[Review] = {
    wsClient.url(reviewUrlFormat.format(businessUnitId)).get()
      .map(resp => getFirstReview(resp.body))
  }
}
