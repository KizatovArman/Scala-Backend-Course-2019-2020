package week6.model

/**
  *
  * @param id unique ID of a movie
  * @param title
  * @param director
  * @param yearOfRelease
  */

case class Movie(id: String, title: String, director: Director, yearOfRelease: Int)
