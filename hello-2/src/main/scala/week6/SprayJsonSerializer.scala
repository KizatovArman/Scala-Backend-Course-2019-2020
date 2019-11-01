package week6

import spray.json.DefaultJsonProtocol
import week6.model.{Director, ErrorResponse, SuccessfulResponse, Movie}

trait SprayJsonSerializer extends DefaultJsonProtocol {

  implicit val directorFormat = jsonFormat4(Director)
  implicit val movieFormat = jsonFormat4(Movie)

  implicit val successfulResponse = jsonFormat2(SuccessfulResponse)
  implicit val errorResponse = jsonFormat2(ErrorResponse)
}
