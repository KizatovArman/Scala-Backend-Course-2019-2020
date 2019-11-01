package actor

import akka.actor.{Actor, ActorLogging, Props}
import com.sksamuel.elastic4s.ElasticsearchClientUri
import com.sksamuel.elastic4s.http.ElasticDsl._
import com.sksamuel.elastic4s.http.index.CreateIndexResponse
import com.sksamuel.elastic4s.http.{HttpClient, RequestFailure, RequestSuccess}
import model.{ErrorResponse, Manga, SuccessfulResponse}
import ElasticSerializer.ElasticSerializer

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}


object OnlineShopManager {

  case class CreateManga(manga: Manga)

  case class ReadManga(id: String)

  case class UpdateManga(manga: Manga)

  case class DeleteManga(id: String)

  def props() = Props(new OnlineShopManager)
}



class OnlineShopManager extends Actor with ActorLogging with ElasticSerializer{
  import OnlineShopManager._

  val client = HttpClient(ElasticsearchClientUri("localhost", 9200))

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

  def receive: Receive = {
    case CreateManga(manga) =>
      val cmd = client.execute(indexInto("manga" / "doc").id(manga.id).doc(manga))

      cmd.onComplete {
        case Success(value) =>
          log.info("Manga with ID: {} created.", manga.id)
          sender() ! Right(SuccessfulResponse(201, s"Manga with ID: ${manga.id} created."))
          println(value)

        case Failure(exception) =>
          log.warning(s"Could not create a manga with ID: ${manga.id} because it already exists.")
          sender() ! Left(ErrorResponse(409, s"Manga with ID: ${manga.id} already exists."))
          println(exception.getMessage)
      }

    case ReadManga(id: String) =>
        client.execute { get(id).from("manga" / "_doc") }
          .onComplete {
          case Success(either) =>
            either.map ( e => e.result.to[Manga] ).foreach {
              manga => println(manga)
              log.info("There is a manga with ID: {}.", manga.id)
              sender() ! Right(manga)
            }


          case Failure(fail) =>
            println(fail.getMessage)
            log.error(s"Could not find a manga with ID: ${id} within ReadManga Request.")
            sender() ! Left(ErrorResponse(404, s"Manga with ID: ${id} not found in ReadManga request."))
        }
  }
}
