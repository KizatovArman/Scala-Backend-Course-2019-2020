package Serializer

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import model.TelegramMessage
import spray.json._

trait TMSerializer extends DefaultJsonProtocol with SprayJsonSerializer{
  implicit val messageFormat: RootJsonFormat[TelegramMessage] = jsonFormat2(TelegramMessage)
}
