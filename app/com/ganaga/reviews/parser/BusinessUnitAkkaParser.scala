package com.ganaga.reviews.parser

import akka.NotUsed
import akka.stream.Materializer
import akka.stream.scaladsl.Flow
import com.ganaga.reviews.model.BusinessUnitParserModel
import com.ganaga.reviews.parser.BusinessUnitAkkaParser.businessUnitsPathFormat
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.libs.json.JsValue
import play.api.libs.json.Json

import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

object BusinessUnitAkkaParser {
  val businessUnitsPathFormat = "https://www.trustpilot.com/categories/%s?sort=latest_review"
}
class BusinessUnitAkkaParser @Inject()(implicit executionContext: ExecutionContext, materializer: Materializer) {

  val fetchDocumentForCategoryFlow: Flow[String, Document, NotUsed] =
    Flow[String].mapAsync(4)(getDocumentF)
  val parseBusinessUnitsFlow: Flow[Document, List[BusinessUnitParserModel], NotUsed] =
    Flow[Document].mapAsync(4)(parseDocument)
  val parseRecentlyReviewedFlow: Flow[String, BusinessUnitParserModel, NotUsed] =
    fetchDocumentForCategoryFlow.via(parseBusinessUnitsFlow).mapConcat(identity)

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
