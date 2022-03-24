package myApp

case class Mean(var aggregatedMean: Double = 0.0, var numBatches: Int = 0) { //Default values of zero added so that the case class starts from zero

  def addDataBatch(batchMean: Double): Unit = {
    val oldTotal = aggregatedMean * numBatches
    numBatches += 1
    aggregatedMean = (oldTotal + batchMean)/numBatches
  }
}
