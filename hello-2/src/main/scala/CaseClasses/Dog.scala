package CaseClasses

import Traits.{Animal, Walks}

case class Dog(name: String) extends Walks with Animal{
  override def makeSound(): String = {
    "Whooof"
  }
}
