package com.ganaga.reviews.parser

import akka.NotUsed
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.Materializer
import akka.stream.scaladsl._
import com.ganaga.reviews.parser.TrafficParser.AUTHORITY_HEADER_NAME
import com.ganaga.reviews.parser.TrafficParser.AUTHORITY_HEADER_VALUE
import com.ganaga.reviews.parser.TrafficParser.COOKIE_VSTAT_NAME
import com.ganaga.reviews.parser.TrafficParser.COOKIE_VSTAT_VALUE
import com.ganaga.reviews.parser.TrafficParser.trafficUrlFormat
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

case class TrafficAkkaParser @Inject()(implicit executionContext: ExecutionContext) {



//  def parseS(domains: List[String]) = {
//    Source(domains)
//      .mapAsyncUnordered(4)(d => getDocument(d).map(doc => (d, doc)))
//      .map{ case (dmn, doc) => (dmn, doc.select("#MONTHLY_VISITS").first().attr("data-smvisits").toLong)}
//      .runWith(Sink.collection[(String, Long), List[(String, Long)]])
//  }

  def parseTrafficForDomain(domain: String): Future[(String, Long)] = {
    getDocument(domain)
      .map(doc => (domain, doc.select("#MONTHLY_VISITS").first().attr("data-smvisits").toLong))
  }

//
//  def parseFlow(): Flow[(String, Document), (String, Long), NotUsed] = {
//    Flow.fromFunction{ case (domain, doc) =>
//      val traffic = doc.select("#MONTHLY_VISITS").first().attr("data-smvisits").toLong
//      (domain, traffic)
//    }
//  }




//  def parseTraffic(domains: List[String]): Map[String, Long] = {
//    domains.map(d => parseTraffic(d)).toMap
//  }
//
//
//  def parseTraffic(domain: String): (String, Long) = {
//    val connection = getConnection(domain)
//    val traffic = connection.get().select("#MONTHLY_VISITS").first().attr("data-smvisits").toLong
//    (domain, traffic)
//  }

//  def parseTrafficF(domains: List[String]): Future[Map[String, Long]] = {
//    val res = domains.map(parseTrafficF)
//    Future.sequence(res).map(_.toMap)
//  }
//
//  def parseTrafficF(domain: String) = getDocument(domain).map{ doc =>
//    val traffic = doc.select("#MONTHLY_VISITS").first().attr("data-smvisits").toLong
//    (domain, traffic)
//  }

  private def getDocument(domain: String): Future[Document] = Future {
    val url = trafficUrlFormat.format(domain)
    Jsoup.connect(url).header(AUTHORITY_HEADER_NAME, AUTHORITY_HEADER_VALUE)
      .cookie(COOKIE_VSTAT_NAME, COOKIE_VSTAT_VALUE).get()
  }

//  private def getConnection(domain: String): Connection = {
//    val url = trafficUrlFormat.format(domain)
//    Jsoup.connect(url).header(AUTHORITY_HEADER_NAME, AUTHORITY_HEADER_VALUE)
//      .cookie(COOKIE_VSTAT_NAME, COOKIE_VSTAT_VALUE)
//  }
}
