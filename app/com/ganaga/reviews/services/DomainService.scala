package com.ganaga.reviews.services

import com.ganaga.reviews.model.BusinessUnitEntity
import com.ganaga.reviews.model.DomainData
import com.ganaga.reviews.store.BusinessUnitsStore
import com.ganaga.reviews.store.TrafficStore

class DomainService() {

  def topTenDomains(): List[DomainData] = {
    val topTenUnits = BusinessUnitsStore.getAllBusinessUnits().values
      .toList.sortBy(bu => bu.latestReviewCount.getOrElse(0))(Ordering[Int].reverse).take(10)
    topTenUnits.map(unit => mapToDomainData(unit))
      .sortBy(d => (d.latestReviewCount, d.traffic))(Ordering[(Int, Long)].reverse)
  }

  private def mapToDomainData(bu: BusinessUnitEntity): DomainData = {
    DomainData(
      bu.identifyingName,
      bu.latestReview,
      bu.latestReviewCount.getOrElse(0),
      bu.totalNumberOfReviews,
      TrafficStore.getTraffic(bu.identifyingName).getOrElse(0)
    )
  }
}
