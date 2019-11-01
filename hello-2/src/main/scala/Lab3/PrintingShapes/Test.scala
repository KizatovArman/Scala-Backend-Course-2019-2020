package Lab3.PrintingShapes

import Lab3.PrintingShapes
import Lab3.ShapingUp.{Circle, Square}
import Lab3.ShapingUp.Rectangle

sealed trait Shape{
  def sides: Int
  def perimeter: Double
  def area: Double
}

case class Circle(radius: Double) extends Shape{
  val sides = 1
  val perimeter = 2*math.Pi * radius
  val area = math.Pi * radius * radius
}

sealed trait Rectangular extends Shape{
  def width: Double
  def height: Double
  val sides = 4
  val perimeter = 2 * (width + height)
  val area = width * height
}

case class Square(size: Double) extends Rectangular {
  val width = size
  val height = size
}

case class Rectangle(val width: Double, val height: Double) extends Rectangular {

}

object Draw {
  def apply(shape: Shape): String = shape match {
    case Rectangle(width, height) =>
      s"A rectangle of width ${width}cm and height ${height}cm"

    case Square(size) =>
      s"A square of size ${size}cm"

    case Circle(radius) =>
      s"A circle of radius ${radius}cm"
  }
}

object Test extends App {
  println(Draw(Lab3.PrintingShapes.Circle(36)))
  println(Draw(PrintingShapes.Rectangle(32,32)))
  println(Draw(PrintingShapes.Square(4)))
}
