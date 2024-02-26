package com.ganaga.reviews.services

import akka.NotUsed
import akka.stream.Materializer
import akka.stream.scaladsl.Flow
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source
import com.ganaga.reviews.model.BusinessUnitEntity
import com.ganaga.reviews.parser.TrafficAkkaParser
import com.ganaga.reviews.store.TrafficStore

import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

case class TrafficServiceAkka @Inject()(parser: TrafficAkkaParser)(implicit executionContext: ExecutionContext, materializer: Materializer) {

  val mapToDomainFlow = Flow[BusinessUnitEntity].map(_.identifyingName)
  val parseTrafficFlow = Flow[String].mapAsyncUnordered(4)(parser.parseTrafficForDomain)
  val trafficToMapSink = Sink.collection[(String, Long), Map[String, Long]]


  def updateTrafficF(source: Source[BusinessUnitEntity, NotUsed]) = {
    source
      .via(mapToDomainFlow)
      .via(parseTrafficFlow)
      .runWith(trafficToMapSink)
      .foreach(traffic => TrafficStore.newTraffic(traffic))
  }

}
