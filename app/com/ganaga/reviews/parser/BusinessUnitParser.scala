package com.ganaga.reviews.parser

import com.ganaga.reviews.model.BusinessUnitParserModel
import com.ganaga.reviews.parser.BusinessUnitParser._
import org.jsoup.Jsoup
import play.api.libs.json.JsValue
import play.api.libs.json.Json

object BusinessUnitParser {
  val businessUnitsPathFormat = "https://www.trustpilot.com/categories/%s?sort=latest_review"
}

case class BusinessUnitParser() {


  def parseRecentlyReviewed(categoriesIds: List[String]): List[BusinessUnitParserModel] = {
    categoriesIds.flatMap(catId => parseRecentlyReviewed(catId))
  }

  /**
   *  Fetches top 20 recently reviewed stores for category
   */
  def parseRecentlyReviewed(categoryId: String): List[BusinessUnitParserModel] = {
    import BusinessUnitParserModel._
    val url = businessUnitsPathFormat.format(categoryId)
    val json = Jsoup.connect(url).get().select("#__NEXT_DATA__").first().data()
    val jsonValue: JsValue = Json.parse(json)
    val businessUnits = (jsonValue\\"businesses").toList.head.as[List[BusinessUnitParserModel]]
    businessUnits
  }

}
