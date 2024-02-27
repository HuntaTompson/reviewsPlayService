package com.ganaga.reviews.parser

import akka.NotUsed
import akka.stream.Materializer
import akka.stream.scaladsl.Flow
import com.ganaga.reviews.model.BusinessUnitParserModel
import com.ganaga.reviews.parser.BusinessUnitParser.businessUnitsPathFormat
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.libs.json.JsValue
import play.api.libs.json.Json

import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

object BusinessUnitParser {
  val businessUnitsPathFormat = "https://www.trustpilot.com/categories/%s?sort=latest_review"
}
class BusinessUnitParser @Inject()(implicit executionContext: ExecutionContext, materializer: Materializer) {

  val parseRecentlyReviewedFlow: Flow[String, BusinessUnitParserModel, NotUsed] =
    Flow[String]
      .mapAsyncUnordered(4)(getDocumentF)
      .mapAsyncUnordered(4)(parseDocument)
      .mapConcat(identity)

  def parseDocument(doc: Document): Future[List[BusinessUnitParserModel]] = Future {
    val json = doc.select("#__NEXT_DATA__").first().data()
    val jsonValue: JsValue = Json.parse(json)
    val businessUnits = (jsonValue\\"businesses").toList.head.as[List[BusinessUnitParserModel]]
    businessUnits
  }

  def getDocumentF(categoryId: String): Future[Document] = Future{
    val url = businessUnitsPathFormat.format(categoryId)
    Jsoup.connect(url).get()
  }

}
