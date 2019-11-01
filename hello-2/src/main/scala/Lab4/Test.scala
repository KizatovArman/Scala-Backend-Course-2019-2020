package Lab4

import java.time.Year

import scala.collection.convert.Wrappers.SeqWrapper


case class Film(name: String,
                yearOfRelease: Int,
                imbdRating: Double)

case class Director(firstName: String,
                    lastName: String,
                    yearOfBirth: Int,
                    films: Seq[Film])


object Test extends App {
  val memento = new Film("Memento", 2000, 8.5)
  val darkKnight = new Film("Dark Knight", 2008, 9.0)
  val inception = new Film("Inception", 2010, 8.8)
  val highPlainsDrifter = new Film("High Plains Drifter", 1973, 7.7)
  val outlawJoseyWales = new Film("The Outlaw Josey Wales", 1976, 7.9)
  val unforgiven = new Film("Unforgiven", 1992, 8.3)
  val granTorino = new Film("Gran Torino", 2008, 8.2)
  val invictus = new Film("Invictus", 2009, 7.4)
  val predator = new Film("Predator", 1987, 7.9)
  val dieHard = new Film("Die Hard", 1988, 8.3)
  val huntForRedOctober = new Film("The Hunt for Red October", 1990, 7.6)
  val thomasCrownAffair = new Film("The Thomas Crown Affair", 1999, 6.8)
  val eastwood = new Director("Clint", "Eastwood", 1930,
    Seq(highPlainsDrifter, outlawJoseyWales, unforgiven, granTorino, invictus))
  val mcTiernan = new Director("John", "McTiernan", 1951,
    Seq(predator, dieHard, huntForRedOctober, thomasCrownAffair))
  val nolan = new Director("Christopher", "Nolan", 1970,
    Seq(memento, darkKnight, inception))
  val someGuy = new Director("Just", "Some Guy", 1990,
    Seq())
  val directors = Seq(eastwood, mcTiernan, nolan, someGuy)

  def task1(numberOfFilms: Int): Seq[Director] = directors.filter(_.films.length > numberOfFilms)
  println(task1(2))

  def task2(year: Int): Option[Director] = directors.find(_.yearOfBirth < year)
  println(task2(1990))

  def task3(year: Int, numberOfFilms: Int): Seq[Director] = {
    val byAge = directors.filter(_.yearOfBirth < year)
    val byNumOfFilms = directors.filter(_.films.length > numberOfFilms)
    byAge.filter(byNumOfFilms.contains)

    directors.filter(d => d.yearOfBirth < year && d.films.length > numberOfFilms)
  }
  println(task3(1950, 2))

  def task4(ascending: Boolean = true): Seq[Director] = {
    if(ascending) {
      directors.sortWith((x,y) => x.yearOfBirth < y.yearOfBirth)
    }
    else {
      directors.sortWith((x,y) => x.yearOfBirth > y.yearOfBirth)
    }
  }
  println(task4())

  println(nolan.films.map(_.name)) // task5

  println(directors.flatMap(director => director.films.map(film => film.name))) // task6

  println(mcTiernan.films.sortWith { (x,y) => x.yearOfRelease < y.yearOfRelease }.headOption) // task7

  println(directors.flatMap(director => director.films).sortWith((x,y) => x.imbdRating > y.imbdRating)) // task8

  val films = directors.flatMap(director => director.films)
  println(films.foldLeft(0.0)((sum, film) => sum + film.imbdRating)/films.length) // task9

  directors.foreach {
    director => director.films.foreach {
      film => println(s"Tonight! ${film.name} by ${director.firstName} ${director.lastName}!")
    }} // task10

  directors.flatMap(director => director.films).sortWith((x,y) => x.yearOfRelease < y.yearOfRelease).headOption // task11
}
