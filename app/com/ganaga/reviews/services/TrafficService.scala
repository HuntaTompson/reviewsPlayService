package com.ganaga.reviews.services

import akka.NotUsed
import akka.stream.Materializer
import akka.stream.scaladsl.Flow
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source
import com.ganaga.reviews.model.BusinessUnitEntity
import com.ganaga.reviews.parser.TrafficParser
import com.ganaga.reviews.store.BusinessUnitsStore
import com.ganaga.reviews.store.TrafficStore

import javax.inject.Inject
import scala.concurrent.ExecutionContext

case class TrafficService @Inject()(parser: TrafficParser)(implicit executionContext: ExecutionContext, materializer: Materializer) {
  val entityTrafficFlow: Flow[BusinessUnitEntity, (BusinessUnitEntity, Long), NotUsed] =
    Flow[BusinessUnitEntity].mapAsync(4)(parser.parseBusinessUnitTraffic)

  def updateTraffic() = {
    Source.fromIterator(() => BusinessUnitsStore.getAllBusinessUnits().valuesIterator)
      .via(entityTrafficFlow)
      .map{ case (entity, trfc) => (entity.identifyingName, trfc)}
      .runWith(Sink.collection[(String, Long), Map[String, Long]])
      .map(allTraffic => TrafficStore.newTraffic(allTraffic))
  }

  def getTraffic(businessUnitEntity: BusinessUnitEntity): Long = {
    TrafficStore.getTraffic(businessUnitEntity.identifyingName).getOrElse(0)
  }
}
