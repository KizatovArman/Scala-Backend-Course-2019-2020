package Lab3.IntListLength

sealed trait IntList {
  def length: Int =
    this match {
      case End => 0
      case Node(head, tail) => 1 + tail.length
    }

  def product: Int =
    this match {
      case End => 1
      case Node(head, tail) => head * tail.product
    }

  def double: IntList =
    this match {
      case End => End
      case Node(head, tail) => Node(2 * head, tail.double)
    }
}


case object End extends IntList
case class Node(head: Int, tail: IntList) extends IntList

object Test extends App {
  var intList = Node(1,Node(2,Node(3,Node(4,End))))

  assert(intList.length == 4)
  assert(intList.tail.length == 3)
  assert(End.length == 0)
  assert(intList.product == 1*2*3*4)
  assert(intList.tail.product == 2*3*4)
  assert(End.product == 1)
  assert(intList.double == Node(1*2, Node(2*2,Node(3*2,Node(4*2,End)))))
  assert(intList.tail.double == Node(4,Node(6,Node(8,End))))
  assert(End.double == End)
}
