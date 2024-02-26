package com.ganaga.reviews.controllers

import com.ganaga.reviews.MainTask
import com.ganaga.reviews.services.DomainService
import com.ganaga.reviews.store.TrafficStore
import play.api.libs.json.Json

import javax.inject._
import play.api.mvc._

@Singleton
class DomainController @Inject()(cc: ControllerComponents, domainService: DomainService, mainTask: MainTask) extends AbstractController(cc) {

  def getTopDomains = Action {
    val topTen = domainService.topTenDomains()
    val json = Json.toJson(topTen)
    Ok(json)
  }

  def doUpdate = Action {
    mainTask.doUpdate()
    Ok("Update finished")
  }

}
