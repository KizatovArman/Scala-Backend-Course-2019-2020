package Lab3.ShapingUp

object Test extends App {
  val sq1 = new Square(4);
  val rect1 = new Rectangle(13, 14);
  val circle1 = new Circle(43);
  println(s"Square sq1. Area: ${sq1.area}. Perimeter: ${sq1.perimeter}. Sides: ${sq1.sides}\n");
  println(s"Rectangle rect1. Area: ${rect1.area}. Perimeter: ${rect1.perimeter}. Sides: ${rect1.sides}\n");
  println(s"Circle sq1. Area: ${circle1.area}. Perimeter: ${circle1.perimeter}. Sides: ${circle1.sides}\n");
}
