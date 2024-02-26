package com.ganaga.reviews.parser

import com.ganaga.reviews.model.BusinessUnitEntity
import com.ganaga.reviews.parser.TrafficAkkaParser._
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

object TrafficAkkaParser {
  val trafficUrlFormat = "https://vstat.info/%s"

  val COOKIE_VSTAT_NAME = "vstat_session"
  val COOKIE_VSTAT_VALUE = "O68lcne2YMZAhRU6PP4pV9OwRjtOWMrvhj3KECgt"

  val AUTHORITY_HEADER_NAME = "authority"
  val AUTHORITY_HEADER_VALUE = "web.vstat.info"
}

class TrafficAkkaParser @Inject()(implicit executionContext: ExecutionContext) {

  def parseBusinessUnitTraffic(buEntity: BusinessUnitEntity): Future[(BusinessUnitEntity, Long)] = {
    getDocument(buEntity.identifyingName)
      .map(doc => (buEntity, doc.select("#MONTHLY_VISITS").first().attr("data-smvisits").toLong))
  }

  private def getDocument(domain: String): Future[Document] = Future {
    val url = trafficUrlFormat.format(domain)
    Jsoup.connect(url).header(AUTHORITY_HEADER_NAME, AUTHORITY_HEADER_VALUE)
      .cookie(COOKIE_VSTAT_NAME, COOKIE_VSTAT_VALUE).get()
  }
}
