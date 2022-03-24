package myApp

case class Variance(var aggregatedVariance: Double = 0.0, var numBatches: Int = 0) {

  def addDataBatch(batchVariance: Double, batchSize: Int): Unit = {
    val oldSum = numBatches * batchSize * aggregatedVariance
    numBatches += 1
    aggregatedVariance = (batchVariance + oldSum) / (numBatches * batchSize)
  }
}