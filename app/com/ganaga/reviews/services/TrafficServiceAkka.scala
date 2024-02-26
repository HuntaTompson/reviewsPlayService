package com.ganaga.reviews.services

import akka.stream.scaladsl.Source
import com.ganaga.reviews.parser.TrafficAkkaParser
import com.ganaga.reviews.store.TrafficStore

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

case class TrafficServiceAkka(parser: TrafficAkkaParser)(implicit executionContext: ExecutionContext) {

  def updateTrafficForDomains(domains: List[String]): Future[Map[String, Long]] = {
    parser.parseS(domains).map(allTraffic => TrafficStore.newTraffic(allTraffic.toMap))
  }

}
