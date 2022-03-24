package myApp


import java.io.{FileOutputStream, PrintStream}
import scala.Numeric.Implicits._
import scala.io.Source

object Application extends App {

  val lines: Iterator[String] = Source.fromResource("customerData.csv").getLines()

  val data1 = lines.map(_.split(',').map(_.trim)) // Split each line on the comma and strip out whitespace
  val data2 = data1.map { case Array(x, y) => (x, y.toDouble) } // map each resulting array into a tuple
  val data3 = data2.toSeq // Convert from Iterator to Array
  //  data3.foreach(println)
  val batchSize: Int = 10000
  var rollingMean: Mean = new Mean
  var rollingVariance: Variance = new Variance

  data3.grouped(batchSize).foreach {
    batch =>

      processBatch(batch)
  }



  def processBatch (batch: Seq[(String, Double)]): Unit = {
    // Calculate Mean, Variance and Standard Deviation
    def calculateMean[T: Numeric](xs: Iterable[T]): Double = {
      val newMean = xs.sum.toDouble / xs.size
      rollingMean.addDataBatch(newMean)
      rollingMean.aggregatedMean
    }

    def calculateVariance[T: Numeric](xs: Iterable[T]): Double = {
      val avg = calculateMean(xs)
      val newSum = xs.map(_.toDouble).map(a => math.pow(a - avg, 2)).sum
      rollingVariance.addDataBatch(newSum, batchSize)
      rollingVariance.aggregatedVariance
    }

    def stdDev[T: Numeric](xs: Iterable[T]): Double = math.sqrt(calculateVariance(xs))

    // Look at data3 array and take the item but only look at the second item on the line

    val stdDevCustomerData = stdDev(batch.map(_._2)) * 2 //multiplied by 2 as 1 standard deviation would fall within acceptable parameters

    println("mean " + rollingMean.aggregatedMean.round + " variance " + rollingVariance.aggregatedVariance.round + " stdDev " + stdDevCustomerData.round)

    var problemGamblerList = batch.collect {
      case user: (String, Double)
        if user._2 > rollingMean.aggregatedMean + stdDevCustomerData => user
    }
    problemGamblerList.foreach(println)

    Console.withOut(new PrintStream(new FileOutputStream("identified-problem-gamblers.txt", true))) {
      problemGamblerList.foreach(x => println(rollingMean.numBatches, x._1, x._2.toInt))
    }
    Thread.sleep(2000)
  }
}