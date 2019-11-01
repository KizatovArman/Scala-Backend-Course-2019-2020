//object Boot extends App {
//  println("Hello World")
//}

// This is because static members (methods or fields) do not exist in Scala.
//object Helloword {
//  def main(args: Array[String]): Unit = {
//    println("Hello World!")
//  }
//}

// Funtcions are objects
//object Timer {
//  def oncePerSecond(callback: () => Unit): Unit = {
//    while (true) { callback(); Thread sleep 1000 }
//  }
//  def timeFlies(): Unit = {
//    println("time flies like an arrow...")
//  }
//  def main(args: Array[String]): Unit = {
//    oncePerSecond(timeFlies)
//  }
//}

// Anonymous Functions
//object TimerAnonymous {
//  def oncePerSecond(callback: ()=> Unit): Unit = {
//    while (true) { callback(); Thread sleep 1000}
//  }
//  def main(args: Array[String]): Unit = {
//    oncePerSecond(()=>
//      println("time flies like an arrow...")
//    )
//  }
//}