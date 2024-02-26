package com.ganaga.reviews.store

import com.ganaga.reviews.model.BusinessUnitEntity

object BusinessUnitsStore {
  private var businessUnits: Map[String, BusinessUnitEntity] = Map()

  def saveBusinessUnit(businessUnit: BusinessUnitEntity): Map[String, BusinessUnitEntity] = {
    businessUnits = businessUnits + (businessUnit.businessUnitId -> businessUnit)
    businessUnits
  }


  def getAllBusinessUnits(): Map[String, BusinessUnitEntity] = businessUnits
}
