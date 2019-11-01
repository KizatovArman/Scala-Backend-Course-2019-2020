package actor

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.sksamuel.elastic4s.ElasticsearchClientUri
import com.sksamuel.elastic4s.http.ElasticDsl._
import com.sksamuel.elastic4s.http.index.CreateIndexResponse
import com.sksamuel.elastic4s.http.{HttpClient, RequestFailure, RequestSuccess}
import model.{Author, ErrorResponse, Manga, SuccessfulResponse}
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

      val cmd = client.execute(indexInto("manga" / "doc").id(manga.id).doc(manga))

      cmd.onComplete {
        case Success(value) =>
          println(value)
          log.info("Manga with ID: {} created.", manga.id)
          tackleResponse(manga, real_sender, false, true, 201, s"Manga with ID: ${manga.id} created.")

        case Failure(exception) =>
          println(exception.getMessage)
          log.warning(s"Could not create a manga with ID: ${manga.id} because it already exists.")
          tackleResponse(manga, real_sender, false, false, 409, s"Manga with ID: ${manga.id} already exists.")
      }

    case ReadManga(id: String) =>
      val real_sender = sender()

      val author1 = Author("AAAAA", "BBBBB")
      val manga1 = Manga("id-inf", "None", 9999, "None", author1)

        client.execute { get(id).from("manga" / "_doc") }
          .onComplete {
          case Success(either) =>
            either.map ( e => e.result.to[Manga] ).foreach {
              manga => println(manga)
              log.info("There is a manga with ID: {}.", manga.id)
              tackleResponse(manga, real_sender, true, true, 200, "Nothing to say")
            }

          // Problems occur to deserialize not found case
          // TODO: Try to understand and fix it
          case Failure(fail) =>
            println(fail.getMessage)
            log.error(s"Could not find a manga with ID: ${id} within ReadManga Request.")
            tackleResponse(manga1, real_sender, false, false, 404, s"Manga with ID: ${id} not found in ReadManga request.")
        }

    case UpdateManga(manga: Manga) =>
      val real_sender = sender()

      val cmd = client.execute(indexInto("manga"/"doc").id(manga.id).doc(manga))

      cmd.onComplete {
        case Success(value) =>
          println(value)
          log.info(s"Manga with ID: ${manga.id} was updated.")
          tackleResponse(manga, real_sender, false, true, 200, s"Updated a movie with ID: ${manga.id}")

        case Failure(exception) =>
          println(exception.getMessage)
          log.error(s"Could not create a manga with ID: ${manga.id} because it already exists.")
          tackleResponse(manga, real_sender, false, false, 409, s"Manga with ID: ${manga.id} already exists.")
      }

    case DeleteManga(id: String) =>
      val real_sender = sender()

      val author1 = Author("CCCCC", "DDDDD")
      val manga1 = Manga("id-inf", "None", 9999, "None", author1)

      client.execute{ delete(id).from("manga"/"_doc")}
      .onComplete {
        case Success(value) =>
          println(value)
          log.info(s"Manga with ID: ${id} has been deleted from database.")
          tackleResponse(manga1, real_sender, false, true, 200, s"Manga with ID: ${id} was deleted.")

        case Failure(exception) =>
          println(exception.getMessage)
          log.error(s"Could not find a manga with ID: ${id} to Delete.")
          tackleResponse(manga1, real_sender, false, false, 404, s"Manga with ID: ${id} not found. Delete is impossible.")
      }
  }
}

// TODO: Deserialization problem when we look for non-existing manga by id. Don't know how to fix it(((
