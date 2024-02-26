package com.ganaga.reviews.services

import akka.NotUsed
import akka.stream.scaladsl.Flow
import com.ganaga.reviews.model.BusinessUnitEntity
import com.ganaga.reviews.parser.TrafficAkkaParser

import javax.inject.Inject
import scala.concurrent.ExecutionContext

case class TrafficService @Inject()(parser: TrafficAkkaParser)(implicit executionContext: ExecutionContext) {
  // As alternative for fetching traffic on every request we can store it somewhere(memory, DB etc.),
  // and update it during MainTask.doUpdate() process.

  val entityTrafficFlow: Flow[BusinessUnitEntity, (BusinessUnitEntity, Long), NotUsed] =
    Flow[BusinessUnitEntity].mapAsync(4)(parser.parseBusinessUnitTraffic)
}
