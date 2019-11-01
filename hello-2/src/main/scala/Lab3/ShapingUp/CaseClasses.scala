package Lab3.ShapingUp

case class Circle(radius: Double) extends ShapeTrait {
  val sides = 1;
  val perimeter = 2*math.Pi * radius
  val area = math.Pi * radius * radius
}

case class Rectangle(width: Double, height: Double) extends ShapeTrait {
  val sides = 4
  val perimeter = 2 * (width + height)
  val area = width * height
}

case class Square(storona: Double) extends ShapeTrait {
  val sides = 4
  val perimeter = 4 * storona
  val area = storona * storona
}
