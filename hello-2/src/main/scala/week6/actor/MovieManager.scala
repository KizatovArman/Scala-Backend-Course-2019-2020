package week6.actor

import akka.actor.{Actor, ActorLogging, Props}
import week6.model.{ErrorResponse, Movie, SuccessfulResponse}


// CRUD
// Companion Object
// props
// messages
object MovieManager {

  // CREATE
  case class CreateMovie(movie: Movie)


  // READ
  case class ReadMovie(id: String)


  // UPDATE
  case class UpdateMovie(movie: Movie)


  // DELETE
  case class DeleteMovie(id:String)


  // PROPS
  def props() = Props(new MovieManager)
}



class MovieManager extends Actor with ActorLogging {

  // import companion object
  import MovieManager._

  var movies: Map[String, Movie] = Map()


  def receive: Receive = {

    case CreateMovie(movie) =>
      movies.get(movie.id) match {

        case Some(existingMovie) =>
          log.warning(s"Could not create a movie with ID: ${movie.id} because it already exists.")
          sender() ! Left(ErrorResponse(409, s"Movie with ID: ${movie.id} already exists."))


        case None =>
          movies = movies + (movie.id -> movie)
          log.info("Movie with ID: {} created.", movie.id)
          sender() ! Right(SuccessfulResponse(201, s"Movie with ID: ${movie.id} created."))
      }


    case msg: ReadMovie =>
      movies.get(msg.id) match {

        case Some(movie) =>
          log.info("There is a movie with ID: {}.", movie.id)
          sender() ! Right(movie)


        case None =>
          log.error(s"Could not find a movie with ID: ${msg.id} within ReadMovie Request.")
          sender() ! Left(ErrorResponse(404, s"Movie with ID: ${msg.id} not found in ReadMovie request."))
      }


    case UpdateMovie(movie) =>
      movies.get(movie.id) match {

        case Some(existingMovie) =>
          movies += (movie.id -> movie)
          log.info(s"Movie with ID: ${movie.id} was updated.")
          sender() ! Right(SuccessfulResponse(200, s"Updated a movie with ID: ${movie.id}"))


        case None =>
          log.error(s"Could not find a movie with ID: ${movie.id} to Update.")
          sender() ! Left(ErrorResponse(404, s"Movie with ID: ${movie.id} not found. Update is impossible."))
      }


    case msg: DeleteMovie =>
      movies.get(msg.id) match {

        case Some(movie) =>
          movies -= msg.id
          log.info("Movie with ID: {} has been deleted from database.", movie.id)
          sender() ! Right(SuccessfulResponse(200, s"Movie with ID: ${movie.id} was deleted."))


        case None =>
          log.error(s"Could not find a movie with ID: ${msg.id} to Delete.")
          sender() ! Left(ErrorResponse(404, s"Movie with ID: ${msg.id} not found. Delete is impossible."))
      }
  }
}
