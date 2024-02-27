package com.ganaga.reviews.services

import com.ganaga.reviews.model.BusinessUnitEntity
import com.ganaga.reviews.store.BusinessUnitsStore

import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

class BusinessUnitQueryService @Inject()(implicit executionContext: ExecutionContext) {

  def topTenUnits(): Future[List[BusinessUnitEntity]] = {
    Future {
      BusinessUnitsStore.getAllBusinessUnits().values
        .toList.sortBy(bu => bu.latestReviewCount.getOrElse(0))(Ordering[Int].reverse).take(10)
    }
  }
}
