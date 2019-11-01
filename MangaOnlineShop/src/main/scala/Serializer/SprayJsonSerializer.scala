package Serializer

import model.{Author, ErrorResponse, Manga, SuccessfulResponse}
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait SprayJsonSerializer extends DefaultJsonProtocol {
  implicit val authorFormat: RootJsonFormat[Author] = jsonFormat2(Author)
  implicit val mangaFormat: RootJsonFormat[Manga] = jsonFormat5(Manga)

  implicit val successfulResponse: RootJsonFormat[SuccessfulResponse] = jsonFormat2(SuccessfulResponse)
  implicit val errorResponse: RootJsonFormat[ErrorResponse] = jsonFormat2(ErrorResponse)
}
