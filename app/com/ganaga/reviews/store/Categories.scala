package com.ganaga.reviews.store

object Categories {

  val categoriesNames: List[String] = List("Electronics Store", "Jewelry Store", "Clothing Store", "Computer Store")

  val categoriesIds: List[String] = categoriesNames.map(s => s.toLowerCase.replace(" ", "_"))
}
