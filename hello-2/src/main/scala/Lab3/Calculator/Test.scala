package Lab3.Calculator

sealed trait Calculation

case class Success(result: Int) extends Calculation
case class Failure(message: String) extends Calculation

object Test extends App{

}
