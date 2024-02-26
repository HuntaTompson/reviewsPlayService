package com.ganaga.reviews.controllers

import com.ganaga.reviews.MainTask
import com.ganaga.reviews.services.DomainService
import play.api.libs.json.Json
import play.api.mvc._

import javax.inject._
import scala.concurrent.ExecutionContext

@Singleton
class DomainController @Inject()(cc: ControllerComponents, domainService: DomainService, mainTask: MainTask)
                                (implicit executionContext: ExecutionContext) extends AbstractController(cc) {

  def getTopDomains = Action.async {
    domainService.topTenDomains().map(topTen => Ok(Json.toJson(topTen)))
  }

  def doUpdate = Action {
    mainTask.doUpdate()
    Ok("Update finished")
  }

}
