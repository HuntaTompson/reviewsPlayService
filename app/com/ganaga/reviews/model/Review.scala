package com.ganaga.reviews.model

import play.api.libs.functional.syntax._
import play.api.libs.json.JsPath
import play.api.libs.json.Reads
import play.api.libs.json.Reads._
import play.api.libs.json.Writes

import java.time.LocalDateTime

object Review {
  implicit val reviewReads: Reads[Review] = (
    (JsPath \ "id").read[String] and
      (JsPath \ "text").read[String] and
        (JsPath \ "rating").read[Int] and
          (JsPath \ "date" \ "createdAt").read[LocalDateTime]
    )(Review.apply _)

  implicit val reviewWrites: Writes[Review] = (
    (JsPath \ "id").write[String] and
      (JsPath \ "text").write[String] and
        (JsPath \ "rating").write[Int] and
          (JsPath \ "createdAt").write[LocalDateTime]
    )(r => (r.reviewId, r.text, r.rating, r.createdAt))

}

case class Review(reviewId: String, text: String, rating: Int, createdAt: LocalDateTime)
