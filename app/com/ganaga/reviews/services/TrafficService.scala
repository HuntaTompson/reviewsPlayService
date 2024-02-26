package com.ganaga.reviews.services

import com.ganaga.reviews.model.BusinessUnitEntity
import com.ganaga.reviews.parser.TrafficParser
import com.ganaga.reviews.store.TrafficStore

case class TrafficService(parser: TrafficParser) {

  def updateTrafficForDomains(domains: List[String]) = {
    val allTraffic = parser.parseTraffic(domains)
    TrafficStore.newTraffic(allTraffic)
  }

  def updateTrafficForBusinessUnits(bus: List[BusinessUnitEntity]) = {
    val domains = bus.map(_.identifyingName)
    updateTrafficForDomains(domains)
  }
}
