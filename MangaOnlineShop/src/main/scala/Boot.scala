import Serializer.SprayJsonSerializer
import akka.actor.ActorSystem
import akka.pattern.ask
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.stream.{ActorMaterializer, Materializer}
import akka.util.Timeout
import model.{ErrorResponse, Manga, Response, SuccessfulResponse}
import actor.{OnlineShopManager}

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.concurrent.duration._


object Boot extends App with SprayJsonSerializer {

  implicit val system: ActorSystem = ActorSystem("manga-service")
  implicit val materializer: Materializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  implicit val timeout: Timeout = Timeout(10.seconds)

  val mangaManager = system.actorOf(OnlineShopManager.props(), "manga-manager")

  val route =
    path("healthcheck") {
      get {
        complete {
          "OK"
        }
      }
    } ~
    pathPrefix("my-manga-shop") {
      path("manga" / Segment) { movieId =>
        get {
          complete {
            (mangaManager ? OnlineShopManager.ReadManga(movieId)).mapTo[Either[ErrorResponse, Manga]]
          }
        }
      } ~
//          path("movie"/ Segment) { movieId =>
//            delete {
//              complete {
//                (mangaManager ? actor.OnlineShopManager.DeleteMovie(movieId)).mapTo[Either[ErrorResponse, SuccessfulResponse]]
//              }
//            }
//          } ~
      path("manga") {
        post {
          entity(as[Manga]) { movie =>
            complete {
              (mangaManager ? OnlineShopManager.CreateManga(movie)).mapTo[Either[ErrorResponse, SuccessfulResponse]]
            }
          }
        }
      }
//          path("movie") {
//            put {
//              entity(as[Movie]) { movie =>
//                complete {
//                  (movieManager ? MovieManager.UpdateMovie(movie)).mapTo[Either[ErrorResponse, SuccessfulResponse]]
//                }
//              }
//            }
//          }
      }


  val bindingFuture = Http().bindAndHandle(route, "0.0.0.0", 8080)
}
