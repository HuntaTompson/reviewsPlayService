package com.ganaga.reviews.services

import akka.NotUsed
import akka.stream.Materializer
import akka.stream.scaladsl.Flow
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source
import com.ganaga.reviews.model.BusinessUnitEntity
import com.ganaga.reviews.model.DomainData

import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class DomainService @Inject()(buQueryService: BusinessUnitQueryService, trafficService: TrafficService)(implicit executionContext: ExecutionContext, materializer: Materializer) {

  val trafficToDomainDataFlow: Flow[(BusinessUnitEntity, Long), DomainData, NotUsed] =
    Flow[(BusinessUnitEntity, Long)].map{ case (unit, traffic) => mapToDomainData(unit, traffic)}

  def topTenDomains(): Future[List[DomainData]] = {
    val sourceF = buQueryService.topTenUnitsSource()
    val topDomainDataF =
      Source.futureSource(sourceF)
      .via(trafficService.entityTrafficFlow)
      .via(trafficToDomainDataFlow)
      .runWith(Sink.collection[DomainData, List[DomainData]])

    topDomainDataF.map(data => data.sortBy(d => (d.latestReviewCount, d.traffic))(Ordering[(Int, Long)].reverse))
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
