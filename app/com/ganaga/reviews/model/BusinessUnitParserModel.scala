package com.ganaga.reviews.model

import play.api.libs.functional.syntax._
import play.api.libs.json.JsPath
import play.api.libs.json.Reads
import play.api.libs.json.Reads._

object BusinessUnitParserModel {

  implicit val businessUnitReads: Reads[BusinessUnitParserModel] = (
    (JsPath \ "businessUnitId").read[String] and
      (JsPath \ "identifyingName").read[String] and
        (JsPath \ "displayName").read[String] and
          (JsPath \ "numberOfReviews").read[Int]
    )(BusinessUnitParserModel.apply _)
}

case class BusinessUnitParserModel(businessUnitId: String, identifyingName: String, displayName: String, numberOfReviews: Int)
