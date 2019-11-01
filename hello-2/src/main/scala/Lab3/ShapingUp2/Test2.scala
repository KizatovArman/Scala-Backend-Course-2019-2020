package Lab3.ShapingUp2

import Lab3.ShapingUp.ShapeTrait

sealed trait Rectangular extends ShapeTrait {
  def width: Double
  def height: Double
  val sides = 4
  val perimeter = 2 * (width + height)
  val area = width * height
}

case class Square(storona: Double) extends Rectangular {
  val width = storona
  val height = storona
}

case class Rectangle(val width: Double, val height: Double) extends Rectangular {}

object Test2 extends App {

}
