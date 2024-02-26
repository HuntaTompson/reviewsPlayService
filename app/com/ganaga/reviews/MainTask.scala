package com.ganaga.reviews

import akka.actor.ActorSystem
import com.ganaga.reviews.services.BusinessUnitProcessService
import com.ganaga.reviews.services.TrafficService

import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt


class MainTask @Inject()(buService: BusinessUnitProcessService, trafficService: TrafficService, actorSystem: ActorSystem)(implicit executionContext: ExecutionContext) {
  actorSystem.scheduler.scheduleAtFixedRate(initialDelay = 10.seconds, interval = 1.minute) { () =>
    doUpdate()
  }

  def doUpdate() = {
    println("Starting MainTask")
    val l1 = System.currentTimeMillis()
    for {
      _ <- buService.updateRecentlyReviewed()
      l2 = System.currentTimeMillis()
      _ = println(s"updateRecentlyReviewed time: ${l2 - l1}")
      l3 = System.currentTimeMillis()
      _ <- trafficService.updateTraffic()
      l4 = System.currentTimeMillis()
      _ = println(s"updateTraffic time: ${l4 - l3}")
    } println("MainTask finished")
  }
}
