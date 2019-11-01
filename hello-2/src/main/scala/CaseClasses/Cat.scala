package CaseClasses

import Traits.{Animal, Walks}

case class Cat(name: String) extends Walks with Animal{
  override def makeSound(): String = {
    "Miiyaaau"
  }
}
