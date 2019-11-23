import Serializer.{SprayJsonSerializer, TMSerializer}
import akka.actor.ActorSystem
import akka.pattern.ask
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.stream.{ActorMaterializer, Materializer}
import akka.util.Timeout
import model._
import actor.OnlineShopManager
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.concurrent.duration._
import scala.util.{Failure, Success}
import akka.http.scaladsl.marshalling.Marshal
import com.typesafe.config.{Config, ConfigFactory}


object Boot extends App with SprayJsonSerializer with TMSerializer {


  implicit val system: ActorSystem = ActorSystem("manga-service")
  implicit val materializer: Materializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  implicit val timeout: Timeout = Timeout(10.seconds)

  val mangaManager = system.actorOf(OnlineShopManager.props(), "manga-manager")
  val log = LoggerFactory.getLogger("Boot")


  val route =
    path("healthcheck") {
      get {
        complete {
          "OK"
        }
      }
    } ~
    pathPrefix("my-manga-shop") {
      path("manga" / Segment) { mangaId =>
        get {
          complete {
            (mangaManager ? OnlineShopManager.ReadManga(mangaId)).mapTo[Either[ErrorResponse, Manga]]
          }
        }
      } ~
      path("manga"/ Segment) { mangaId =>
        delete {
          complete {
            (mangaManager ? actor.OnlineShopManager.DeleteManga(mangaId)).mapTo[Either[ErrorResponse, SuccessfulResponse]]
          }
        }
      } ~
      path("manga") {
        post {
          entity(as[Manga]) { manga =>
            complete {
              (mangaManager ? OnlineShopManager.CreateManga(manga)).mapTo[Either[ErrorResponse, SuccessfulResponse]]
            }
          }
        }
      } ~
      path("manga") {
        put {
          entity(as[Manga]) { manga =>
            complete {
              (mangaManager ? OnlineShopManager.UpdateManga(manga)).mapTo[Either[ErrorResponse, SuccessfulResponse]]
            }
          }
        }
      }
    }


  val bindingFuture = Http().bindAndHandle(route, "0.0.0.0", 8080)
}
