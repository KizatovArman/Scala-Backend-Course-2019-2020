package Lab3.ShapingUp2

import Lab3.ShapingUp.ShapeTrait

class CaseClasses {

}

case class Circle(radius: Double) extends ShapeTrait {
  val sides = 1;
  val perimeter = 2*math.Pi * radius
  val area = math.Pi * radius * radius
}



