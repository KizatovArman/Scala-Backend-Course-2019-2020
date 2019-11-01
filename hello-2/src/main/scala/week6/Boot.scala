package week6

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.stream.{ActorMaterializer, Materializer}
import akka.util.Timeout
import week6.actor.{MovieManager, TestBot}
import week6.model.{ErrorResponse, Movie, Response, SuccessfulResponse}

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.concurrent.duration._


object Boot extends App with SprayJsonSerializer {

  implicit val system: ActorSystem = ActorSystem("movie-service")
  implicit val materializer: Materializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  implicit val timeout: Timeout = Timeout(10.seconds)

  val movieManager = system.actorOf(MovieManager.props(), "movie-manager")

  val route =
    path("healthcheck") {
      get {
        complete {
          "OK"
        }
      }
    } ~
    pathPrefix("kbtu-cinema") {
      path("movie" / Segment) { movieId =>
        get {
          complete {
            (movieManager ? MovieManager.ReadMovie(movieId)).mapTo[Either[ErrorResponse, Movie]]
          }
        }
      } ~
      path("movie"/ Segment) { movieId =>
        delete {
          complete {
            (movieManager ? MovieManager.DeleteMovie(movieId)).mapTo[Either[ErrorResponse, SuccessfulResponse]]
          }
        }
      } ~
      path("movie") {
        post {
          entity(as[Movie]) { movie =>
            complete {
              (movieManager ? MovieManager.CreateMovie(movie)).mapTo[Either[ErrorResponse, SuccessfulResponse]]
            }
          }
        }
      } ~
      path("movie") {
        put {
          entity(as[Movie]) { movie =>
            complete {
              (movieManager ? MovieManager.UpdateMovie(movie)).mapTo[Either[ErrorResponse, SuccessfulResponse]]
            }
          }
        }
      }
    }




  val bindingFuture = Http().bindAndHandle(route, "0.0.0.0", 8080)


//  val testBot = system.actorOf(TestBot.props(movieManager), "test-bot")
//
//  // test create
//  testBot ! TestBot.TestCreate
//
//  // test conflict
//  testBot ! TestBot.TestConflict
//
//  // test read
//  testBot ! TestBot.TestRead
//
//  // test update
//  testBot ! TestBot.TestUpdate
//
//  // test movie
//  testBot ! Movie("5", "Akka akka Scala", Director("6", "Scala", None, "Tutorial"), 1999)
//
//  // test delete
//  testBot ! TestBot.TestDelete
//
//  // test not found
//  testBot ! TestBot.TestNotFound
}