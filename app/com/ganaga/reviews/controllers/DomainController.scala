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

  /** Data update is automatically triggered every 5 minutes. But you can also start is via this endpoint
   * */
  def doUpdate = Action {
    mainTask.doUpdate()
    Ok("update in process")
  }

}
