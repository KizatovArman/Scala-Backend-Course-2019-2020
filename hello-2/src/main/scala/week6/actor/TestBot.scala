package week6.actor

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import week6.model.{Director, ErrorResponse, Movie, SuccessfulResponse}

object TestBot {

  case object TestCreate

  case object TestConflict

  case object TestRead

  case object TestNotFound

  case object TestUpdate

  case object TestDelete

  def props(manager: ActorRef) = Props(new TestBot(manager))
}

class TestBot(manager: ActorRef) extends Actor with ActorLogging {
  import TestBot._

  override def receive: Receive = {

    case TestCreate =>
      manager ! MovieManager.CreateMovie(Movie("1", "Joker", Director("dir-1", "Todd", None, "Philips"), 2019))
      manager ! MovieManager.CreateMovie(Movie("2", "Charlie's Angels", Director("dir-2", "Ivan", None, "Ivanov"), 2019))
      manager ! MovieManager.CreateMovie(Movie("3", "Iron Man", Director("dir-3", "Akka", None, "Actor"), 2008))
      manager ! MovieManager.CreateMovie(Movie("4", "Iron Man 3", Director("dir-3", "Akka", None, "Actor"), 2014))
      manager ! MovieManager.CreateMovie(Movie("5", "Aquaman", Director("dir-10", "Tom", None, "Cruze"), 2018))


    case TestConflict =>
      manager ! MovieManager.CreateMovie(Movie("2", "Test Test", Director("dir-2", "Ivan", None, "Ivanov"), 2019))
      manager ! MovieManager.DeleteMovie("6")
      manager ! MovieManager.UpdateMovie(Movie("10", "Akka akka Scala", Director("6", "Scala", None, "Tutorial"), 1999))


    case TestRead =>
      manager ! MovieManager.ReadMovie("1")
      manager ! MovieManager.ReadMovie("3")
      manager ! MovieManager.ReadMovie("4")


    case TestUpdate =>
      manager ! MovieManager.UpdateMovie(Movie("3", "Iron Man 2", Director("dir-3", "Akka", None, "Actor"), 2008))


    case TestDelete =>
      manager ! MovieManager.DeleteMovie("5")


    case TestNotFound =>
      manager ! MovieManager.ReadMovie("10")
      manager ! MovieManager.UpdateMovie(Movie("15", "Zdarova", Director("dir-50", "Taika", None, "Waititi"), 2020))
      manager ! MovieManager.DeleteMovie("222")


    case SuccessfulResponse(status, msg) =>
      log.info("Received Successful Response with status: {} and message: {}", status, msg)


    case ErrorResponse(status, msg) =>
      log.warning("Received Error Response with status: {} and message: {}", status, msg)


    case movie: Movie =>
      log.info("Received movie: [{}]", movie)

  }
}