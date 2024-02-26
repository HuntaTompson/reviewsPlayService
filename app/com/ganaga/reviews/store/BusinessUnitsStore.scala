package com.ganaga.reviews.store

import akka.stream.scaladsl.Source
import com.ganaga.reviews.model.BusinessUnitEntity

object BusinessUnitsStore {
  private var businessUnits: Map[String, BusinessUnitEntity] = Map()

  val businessUnitsSource = Source.fromIterator(() => businessUnits.valuesIterator)

  def saveBusinessUnit(businessUnit: BusinessUnitEntity): Map[String, BusinessUnitEntity] = {
    businessUnits = businessUnits + (businessUnit.businessUnitId -> businessUnit)
    businessUnits
  }


  def getAllBusinessUnits(): Map[String, BusinessUnitEntity] = businessUnits
}
