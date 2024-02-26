package com.ganaga.reviews

import akka.actor.ActorSystem
import com.ganaga.reviews.services.BusinessUnitProcessService

import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt


class MainTask @Inject()(buService: BusinessUnitProcessService, actorSystem: ActorSystem)(implicit executionContext: ExecutionContext) {
  actorSystem.scheduler.scheduleAtFixedRate(initialDelay = 10.seconds, interval = 5.minute) { () =>
    doUpdate()
  }

  def doUpdate() = {
    println("Starting MainTask")
    val updateF = buService.updateRecentlyReviewedF()
    updateF.foreach(_ => println("MainTask finished"))
  }
}
