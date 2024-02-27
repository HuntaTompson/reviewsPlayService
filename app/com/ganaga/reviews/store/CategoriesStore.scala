package com.ganaga.reviews.store

import akka.NotUsed
import akka.stream.scaladsl.Source

object CategoriesStore {

  val categoriesNames: List[String] = List("Electronics Store", "Jewelry Store", "Clothing Store")

  val categoriesIds: List[String] = categoriesNames.map(s => s.toLowerCase.replace(" ", "_"))

  val categoriesSource: Source[String, NotUsed] = Source(categoriesIds)
}
