package myApp

import scala.io.Source

object topology extends App {

  var customerName = Source.fromResource("myApp/customerName").getLines.toList

  var customerSpinData = Source.fromResource("myApp/customerSpinData").getLines.map(_.toInt).toList

  var customer = customerName zip customerSpinData

  println(customer.filter(x=>x._2 > 1999))

//  var transformData = customer.map(x => (x._1, x._2, x._2 > 1999))
//
//  println(transformData.filter(x=>x._3==true))
}
