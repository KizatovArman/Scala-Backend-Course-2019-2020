package actor


import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.sksamuel.elastic4s.ElasticsearchClientUri
import com.sksamuel.elastic4s.http.ElasticDsl._
import com.sksamuel.elastic4s.http.index.CreateIndexResponse
import com.sksamuel.elastic4s.http.{HttpClient, RequestFailure, RequestSuccess}
import model._
import ElasticSerializer.ElasticSerializer
import Serializer.{SprayJsonSerializer, TMSerializer}
import akka.http.scaladsl.model.RequestEntity

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model._
import akka.stream.{ActorMaterializer, Materializer}
import com.typesafe.config.{Config, ConfigFactory}
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.Future
import scala.util.{Failure, Success}



object TelegramManager {

  case class SendWhenCreated(manga: Manga, requestStatus: Int)

  case class SendWhenUpdated(manga: Manga, requestStatus: Int)

  def props() = Props(new TelegramManager)
}


class TelegramManager extends Actor with ActorLogging with ElasticSerializer with TMSerializer with SprayJsonSerializer {
  import TelegramManager._

  implicit val system: ActorSystem = ActorSystem("telegram-bot-service")
  implicit val materializer: Materializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  val token = "938105045:AAHCQ6RLGrTszymRG3VSMFcQMYRpPQM6yYQ"
  val chat_id = -352088280


  def sendMessageByBot(manga: Manga, requestType: Int, requestStatus: Int): Unit = {
    var text = ""

    if(requestType == 1) { // Create Manga case
      text = s"Manga ${manga.title} with ID: ${manga.id} has been created. Status of the request is ${requestStatus}."
    }
    else if(requestType == 2) { // Update Manga case
      text = s"Manga ${manga.title} with ID: ${manga.id} has been updated. Status of the request is ${requestStatus}."
    }

    val msg: TelegramMessage = TelegramMessage(chat_id, text)
    val httpReq = Marshal(msg).to[RequestEntity].flatMap { entity =>
      val request = HttpRequest(HttpMethods.POST, s"https://api.telegram.org/bot$token/sendMessage", Nil, entity)
      log.debug("Request: {}", request)
      Http().singleRequest(request)
    }

    httpReq.onComplete {
      case Success(value) =>
        log.info(s"Response: $value")
        value.discardEntityBytes()

      case Failure(exception) =>
        log.error("error")
    }

    Thread.sleep(5000)
  }


  def receive: Receive = {
    case SendWhenCreated(manga: Manga, requestStatus: Int) =>
      sendMessageByBot(manga, 1, requestStatus)

    case SendWhenUpdated(manga: Manga, requestStatus: Int) =>
      sendMessageByBot(manga, 2, requestStatus)
  }
}
