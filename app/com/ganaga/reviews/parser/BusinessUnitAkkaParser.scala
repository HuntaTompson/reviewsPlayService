package com.ganaga.reviews.parser

import akka.NotUsed
import akka.stream.Materializer
import akka.stream.scaladsl.Flow
import akka.stream.scaladsl.Source
import com.ganaga.reviews.model.BusinessUnitParserModel
import com.ganaga.reviews.parser.BusinessUnitAkkaParser.businessUnitsPathFormat
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.libs.json.JsValue
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

object BusinessUnitAkkaParser {
  val businessUnitsPathFormat = "https://www.trustpilot.com/categories/%s?sort=latest_review"
}

case class BusinessUnitAkkaParser()(implicit executionContext: ExecutionContext, materializer: Materializer) {

  val fetchDocumentForCategoryFlow = Flow[String].mapAsync(4)(getDocumentF)
  val parseBusinessUnitsFlow = Flow[Document].mapAsync(4)(parseDocument)
  val parseRecentlyReviewedFlow: Flow[String, BusinessUnitParserModel, NotUsed] =
    fetchDocumentForCategoryFlow.via(parseBusinessUnitsFlow).mapConcat(identity)

//  def parseRecentlyReviewedS(categoriesIds: List[String]): Future[List[BusinessUnitParserModel]] = {
//    Source(categoriesIds)
//      .mapAsyncUnordered(4)(catId => parseRecentlyReviewedF(catId))
//      .runFold(List[BusinessUnitParserModel]())(_ ++ _)
////    categoriesIds.flatMap(catId => parseRecentlyReviewed(catId))
//  }

//  def parseRecentlyReviewedF(categoryId: String): Future[List[BusinessUnitParserModel]] = {
//    getDocumentF(categoryId).map { doc =>
//      val json = doc.select("#__NEXT_DATA__").first().data()
//      val jsonValue: JsValue = Json.parse(json)
//      val businessUnits = (jsonValue\\"businesses").toList.head.as[List[BusinessUnitParserModel]]
//      businessUnits
//    }
//  }

  def parseDocument(doc: Document): Future[List[BusinessUnitParserModel]] = Future {
    val json = doc.select("#__NEXT_DATA__").first().data()
    val jsonValue: JsValue = Json.parse(json)
    val businessUnits = (jsonValue\\"businesses").toList.head.as[List[BusinessUnitParserModel]]
    businessUnits
  }

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

  def getDocumentF(categoryId: String): Future[Document] = Future{
    val url = businessUnitsPathFormat.format(categoryId)
    Jsoup.connect(url).get()
  }

}
