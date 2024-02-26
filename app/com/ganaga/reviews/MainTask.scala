package com.ganaga.reviews

import akka.actor.ActorSystem
import akka.stream.Materializer
import com.ganaga.reviews.parser.BusinessUnitParser
import com.ganaga.reviews.parser.ReviewParser
import com.ganaga.reviews.parser.TrafficAkkaParser
import com.ganaga.reviews.parser.TrafficParser
import com.ganaga.reviews.services.BusinessUnitService
import com.ganaga.reviews.services.TrafficService
import com.ganaga.reviews.services.TrafficServiceAkka
import com.ganaga.reviews.store.BusinessUnitsStore
import play.api.libs.ws.WSClient

import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt


class MainTask @Inject()(wsClient: WSClient, actorSystem: ActorSystem)(implicit executionContext: ExecutionContext) {
  actorSystem.scheduler.scheduleAtFixedRate(initialDelay = 10.seconds, interval = 1.minute) { () =>
    doUpdate()
  }

  implicit val materializer = Materializer(actorSystem)

  val reviewParser: ReviewParser = ReviewParser()
  val trafficParser: TrafficParser = TrafficParser()
  val trafficParserAkka: TrafficAkkaParser = TrafficAkkaParser()
  val businessUnitParser: BusinessUnitParser = BusinessUnitParser()
  val businessUnitService: BusinessUnitService = BusinessUnitService(wsClient, businessUnitParser, reviewParser)
//  val trafficService: TrafficService = TrafficService(trafficParser)
  val trafficService: TrafficServiceAkka = TrafficServiceAkka(trafficParserAkka)


  def doUpdate() = {
    println("Starting MainTask")
    val updateF = businessUnitService.updateRecentlyReviewed()
    updateF.foreach(_ => {
      val l1 = System.currentTimeMillis()
      trafficService.updateTrafficForDomains(BusinessUnitsStore.getAllBusinessUnits().values.map(_.identifyingName).toList).foreach(_ => {
        val l2 = System.currentTimeMillis()
        println(s"updateTrafficForDomains finished. Time - ${l2 - l1}")
      })
      println("MainTask finished")
    })
  }
}
