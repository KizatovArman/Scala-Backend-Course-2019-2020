package actor

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.sksamuel.elastic4s.ElasticsearchClientUri
import com.sksamuel.elastic4s.http.ElasticDsl._
import com.sksamuel.elastic4s.http.index.CreateIndexResponse
import com.sksamuel.elastic4s.http.{HttpClient, RequestFailure, RequestSuccess}
import model._
import ElasticSerializer.ElasticSerializer
import Serializer.TMSerializer
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.RequestEntity

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import com.typesafe.config.{Config, ConfigFactory}
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.Future
import scala.util.{Failure, Success}


object OnlineShopManager {

  case class CreateManga(manga: Manga)

  case class ReadManga(id: String)

  case class UpdateManga(manga: Manga)

  case class DeleteManga(id: String)

  def props() = Props(new OnlineShopManager)
}



class OnlineShopManager extends Actor with ActorLogging with ElasticSerializer with TMSerializer{
  import OnlineShopManager._

  implicit val system: ActorSystem = ActorSystem("online-shop-manager-service")

  val client = HttpClient(ElasticsearchClientUri("localhost", 9200))

  val token = ""
  val chat_id =

  val telegramBotManager = system.actorOf(TelegramManager.props(), "telegram-manager")


  def createEsIndex() = {
    val cmd: Future[Either[RequestFailure, RequestSuccess[CreateIndexResponse]]] = client.execute{ createIndex("manga")}

    cmd.onComplete {
      case Success(value) =>
        value.foreach {requestSuccess =>
          println(requestSuccess)}

      case Failure(exception) =>
        println(exception.getMessage)
    }
  }


  def tackleResponse(manga: Manga, replyTo: ActorRef, sendManga: Boolean, isSuccessful: Boolean, status: Int, answer: String): Unit ={
    if(sendManga && isSuccessful){
      replyTo ! Right(manga)
    }
    else if(isSuccessful && !sendManga){
      replyTo ! Right(SuccessfulResponse(status, answer))
    }
    else if(!isSuccessful && !sendManga){
      replyTo ! Left(ErrorResponse(status, answer))
    }
  }


  def receive: Receive = {

    case CreateManga(manga) =>
      val real_sender = sender()
      val cmd = client.execute(indexInto("manga" / "_doc").id(manga.id).doc(manga))
      cmd.onComplete {

        case Success(either) =>
          either match {
            case Right(sr) =>
              if(sr.result.result == "created") {
                tackleResponse(manga, real_sender, false, true, 201, s"Manga with ID: ${manga.id} created." )
                log.info("Manga with ID: {} created.", manga.id)
                telegramBotManager ! TelegramManager.SendWhenCreated(manga, sr.status)
              }
              else if(sr.result.result == "updated") {
                tackleResponse(manga, real_sender, false, false, 409, s"Manga with ID: ${manga.id} already exists.")
                log.error(s"Could not create a manga with ID: ${manga.id} because it already exists.")
              }
            case Left(left) =>
              tackleResponse(manga, real_sender, false, false, left.status, "Internal Server Error Occurred!")
              log.error(left.toString)
          }

        case Failure(exception) =>
          log.error(s"Internal server error occurred! ${exception.toString}")
          tackleResponse(manga, real_sender, false, false, 500, s"Internal server error occurred!")
      }

    case ReadManga(id: String) =>
      val real_sender = sender()

      val author1 = Author("AAAAA", "BBBBB")
      val manga1 = Manga("id-inf", "None", 9999, "None", author1)

        client.execute { get(id).from("manga" / "_doc") }.onComplete {
          case Success(either) =>
            either match {
              case Right(sr) =>
                if (sr.result.found) {
                  val manga = sr.result.to[Manga]
                  tackleResponse(manga, real_sender, true, true, 200, s"There is a manga with ID: ${manga.id}")
                  log.info("There is a manga with ID: {}.", manga.id)
                } else {
                  tackleResponse(manga1, real_sender, false, false, 404, s"Can not found manga with specified ID: ${id}")
                  log.error(s"Could not find a manga with ID: ${id} within ReadManga Request.")
                }
//                either.map ( e => e.result.to[Manga] ).foreach {
//                  manga => println(manga)
//                    log.info("There is a manga with ID: {}.", manga.id)
//                    tackleResponse(manga, real_sender, true, true, 200, "Nothing to say")
//                }
              case Left(left) =>
                tackleResponse(manga1, real_sender, false, false, left.status, "Internal Server Error Occurred!")
                log.error(left.toString)
        }
          case Failure(fail) =>
            println(fail.getMessage)
            log.error(s"Internal server error occurred")
            tackleResponse(manga1, real_sender, false, false, 500, s"Interval server error occured!")
        }

    case UpdateManga(manga: Manga) =>
      val real_sender = sender()
      val author1 = Author("AAAAA", "BBBBB")
      val manga1 = Manga("id-inf", "None", 9999, "None", author1)

      val cmd = client.execute(update(manga.id).in("manga" / "_doc").doc(
        "title" -> manga.title,
                "yearOfPublication" -> manga.yearOfPublication,
                "genre" -> manga.genre,
                "author" -> Map(
                  "firstName" -> manga.author.firstName,
                  "secondName" -> manga.author.secondName
                )
      ))

      cmd.onComplete {
        case Success(either) =>
          either match {
            case Right(sr) =>
              if(sr.result.found) {
                tackleResponse(manga, real_sender, false, true, 200, s"Manga with ID: ${manga.id} was updated.")
                log.info(s"Manga with ID: ${manga.id} was updated.")
                telegramBotManager ! TelegramManager.SendWhenUpdated(manga, sr.status)
              } else {
                tackleResponse(manga, real_sender, false, false, 404, s"Can not found manga with specified ID: ${manga.id}")
                log.error(s"Could not find a manga with ID: ${manga.id} within UpdateManga Request.")
              }

            case Left(left) =>
              tackleResponse(manga1, real_sender, false, false, left.status, s"Can not found manga with specified ID: ${manga.id}")
              log.error(left.toString)
          }

        case Failure(fail) =>
          println(fail.getMessage)
          log.error(s"Internal server error occurred")
          tackleResponse(manga, real_sender, false, false, 500, s"Interval server error occured!")
      }

    case DeleteManga(id: String) =>
      val real_sender = sender()

      val author1 = Author("CCCCC", "DDDDD")
      val manga1 = Manga("id-inf", "None", 9999, "None", author1)

      client.execute{ delete(id).from("manga"/"_doc")}.onComplete {
        case Success(either) =>
          either match {
            case Right(sr) =>
              if(sr.result.result != "not_found") {
                tackleResponse(manga1, real_sender, false, true, 200, s"Manga with ID: ${id} was deleted.")
                log.info("Manga with ID: {} has been deleted from database.", id)
              } else if(sr.result.result == "not_found") {
                tackleResponse(manga1, real_sender, false, false, 404, s"Manga with ID: ${id} is not found in DeleteManga request.")
                log.error(s"Could not find a manga with ID: ${id} to Delete.")
              }
            case Left(left) =>
              tackleResponse(manga1, real_sender, false, false, left.status, left.toString)
              log.error(left.toString)
          }

        case Failure(fail) =>
          println(fail.getMessage)
          log.error(s"Internal server error occurred")
          tackleResponse(manga1, real_sender, false, false, 500, s"Interval server error occured!")
      }
  }
}
