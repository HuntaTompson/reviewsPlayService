package com.ganaga.reviews

import com.ganaga.reviews.model.Review

import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset

object Utils {

  val serviceStartTimeUTC = LocalDateTime.now(ZoneOffset.UTC)

  def isReviewDateValid(review: Review): Boolean = {
    val now = LocalDateTime.now(ZoneOffset.UTC)
    val reviewCreated = review.createdAt
    val duration = Duration.between(reviewCreated, now)
    val diff = Math.abs(duration.toMinutes)

    diff < 30 && reviewCreated.isAfter(serviceStartTimeUTC)
  }
}
