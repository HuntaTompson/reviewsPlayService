package com.ganaga.reviews.parser

import com.ganaga.reviews.parser.TrafficParser._
import org.jsoup.Jsoup
import org.jsoup.{Connection => JsoupConnection}

object TrafficParser {
  val trafficUrlFormat = "https://vstat.info/%s"

  val COOKIE_VSTAT_NAME = "vstat_session"
  val COOKIE_VSTAT_VALUE = "O68lcne2YMZAhRU6PP4pV9OwRjtOWMrvhj3KECgt"

  val AUTHORITY_HEADER_NAME = "authority"
  val AUTHORITY_HEADER_VALUE = "web.vstat.info"
}

case class TrafficParser() {

  def parseTraffic(domains: List[String]): Map[String, Long] = {
    domains.map(d => parseTraffic(d)).toMap
  }

  def parseTraffic(domain: String): (String, Long) = {
    val connection = getConnection(domain)
    val traffic = connection.get().select("#MONTHLY_VISITS").first().attr("data-smvisits").toLong
    (domain, traffic)
  }

  private def getConnection(domain: String): JsoupConnection = {
    val url = trafficUrlFormat.format(domain)
    Jsoup.connect(url).header(AUTHORITY_HEADER_NAME, AUTHORITY_HEADER_VALUE)
      .cookie(COOKIE_VSTAT_NAME, COOKIE_VSTAT_VALUE)
  }
}
