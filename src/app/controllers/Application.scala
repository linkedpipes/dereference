package controllers

import javax.inject._
import play.api.libs.json.Json
import play.api.mvc._
import service.DereferenceService

@Singleton
class Application @Inject()(service: DereferenceService) extends Controller {

    def dereference(uri: String) = Action {
        Ok(Json.toJson(service.dereferenceLabels(uri)))
    }

}