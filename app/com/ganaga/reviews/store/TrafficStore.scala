package com.ganaga.reviews.store

object TrafficStore {

  private var store: Map[String, Long] = Map()

  def newTraffic(allTraffic: Map[String, Long]): Map[String, Long] = {
    store = allTraffic
    store
  }

  def getTraffic(domainName: String): Option[Long] = {
    store.get(domainName)
  }

}
