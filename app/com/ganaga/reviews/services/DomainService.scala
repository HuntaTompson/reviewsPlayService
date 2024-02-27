package com.ganaga.reviews.services

import akka.stream.Materializer
import com.ganaga.reviews.model.BusinessUnitEntity
import com.ganaga.reviews.model.DomainData

import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class DomainService @Inject()(buQueryService: BusinessUnitQueryService, trafficService: TrafficService)(implicit executionContext: ExecutionContext, materializer: Materializer) {

  def topTenDomains(): Future[List[DomainData]] = {
    buQueryService.topTenUnits().map { units =>
      val topTenDomains: List[DomainData] = mapToDomainData(units)
      val sorted = topTenDomains.sortBy(d => (d.latestReviewCount, d.traffic))(Ordering[(Int, Long)].reverse)
      sorted
    }
  }

  private def mapToDomainData(buEntities: List[BusinessUnitEntity]): List[DomainData] = {
    for {
      bu <- buEntities
      traffic = trafficService.getTraffic(bu)
    } yield mapToDomainData(bu, traffic)
  }

  private def mapToDomainData(bu: BusinessUnitEntity, traffic: Long): DomainData = {
    DomainData(
      bu.identifyingName,
      bu.latestReview,
      bu.latestReviewCount.getOrElse(0),
      bu.totalNumberOfReviews,
      traffic
    )
  }
}
