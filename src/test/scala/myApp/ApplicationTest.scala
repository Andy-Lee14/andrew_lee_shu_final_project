package myApp

import org.scalatest.{Matchers, WordSpec}

import scala.Numeric.Implicits._
import scala.util.Random


class ApplicationTest extends WordSpec with Matchers {

    def mean[T: Numeric](xs: Iterable[T]): Double = xs.sum.toDouble / xs.size

    "Topology Test" should {

        val firstBatch = (0 to 99).map(e => Random.nextInt(100)).toList
        val secondBatch = (0 to 99).map(e => Random.nextInt(100)).toList
        val thirdBatch = (0 to 99).map(e => Random.nextInt(100)).toList
        val combinedBatch = firstBatch ++ secondBatch ++ thirdBatch
        println(mean(firstBatch))
        println(mean(secondBatch))
        println(mean(thirdBatch))
        println(mean(combinedBatch))
        println((mean(firstBatch)+mean(secondBatch)+mean(thirdBatch))/3)
    }
}