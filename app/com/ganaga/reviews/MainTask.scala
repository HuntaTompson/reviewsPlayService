package com.ganaga.reviews

import akka.actor.ActorSystem
import com.ganaga.reviews.services.BusinessUnitProcessService
import com.ganaga.reviews.services.TrafficService
import play.api.Logger
import play.api.Logging

import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt


class MainTask @Inject()(buService: BusinessUnitProcessService, trafficService: TrafficService, actorSystem: ActorSystem)(implicit executionContext: ExecutionContext) {
  actorSystem.scheduler.scheduleAtFixedRate(initialDelay = 10.seconds, interval = 5.minute) { () =>
    doUpdate()
  }
  val logger: Logger = Logger(this.getClass())

  def doUpdate() = {
    logger.info("Starting data update")
    for {
      _ <- buService.updateRecentlyReviewed()
      _ <- trafficService.updateTraffic()
    } logger.info("Data update finished")
  }
}
