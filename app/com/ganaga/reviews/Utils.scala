package com.ganaga.reviews

import com.ganaga.reviews.model.BusinessUnitEntity

import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset

object Utils {

  def isReviewDateValid(bu: BusinessUnitEntity): Boolean = {
    val now = LocalDateTime.now(ZoneOffset.UTC)
    val reviewCreated = bu.latestReview.createdAt
    val duration = Duration.between(reviewCreated, now)
    val diff = Math.abs(duration.toMinutes)

    diff < 30
  }

  def isReviewNotSame(firstBu: BusinessUnitEntity, secondBu: BusinessUnitEntity) = firstBu.latestReview.reviewId != secondBu.latestReview.reviewId
}
