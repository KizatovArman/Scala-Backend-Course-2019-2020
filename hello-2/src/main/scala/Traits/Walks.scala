package Traits

trait Walks {

  this: Animal =>

  def walk: String = s"${name} is walking"
}
