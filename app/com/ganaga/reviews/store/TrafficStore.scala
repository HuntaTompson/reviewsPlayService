package com.ganaga.reviews.store

object TrafficStore {

  private var store: Map[String, Long] = Map()

  def saveTraffic(domainName: String, traffic: Long): Map[String, Long] = {
    store = store + (domainName -> traffic)
    store
  }

  def newTraffic(allTraffic: Map[String, Long]): Map[String, Long] = {
    store = allTraffic
    store
  }

  def getTraffic(domainName: String): Option[Long] = {
    store.get(domainName)
  }

  def getAllTraffic() = store

}
