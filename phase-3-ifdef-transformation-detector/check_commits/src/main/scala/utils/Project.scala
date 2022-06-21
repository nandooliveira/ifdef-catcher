package ic.ufal.ifdefcatcher
package utils

import com.fasterxml.jackson.annotation.{JsonFormat, JsonProperty}

import java.util.Calendar
import scala.collection.mutable
import scala.io.Source
import scala.util.{Try, Using}

class Project(
               @JsonProperty(required = true) val name: String,
               @JsonProperty(required = true) val url: String,
               val withThreads: Boolean = true,
               val startCommit: String = null,
               val endCommit: String = null,
               @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") val since: Calendar = null,
               val startTag: String = null,
               val endTag: String = null,
               val method: String = "commits",
               @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") val from: Calendar = null,
               @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") val to: Calendar = null
             )

object ProjectFilter {
  var TURN_ON = false
  var FILEPATH = ""

  private val fileList: mutable.HashSet[String] = mutable.HashSet[String]()

  var totalCommits : Long = 0
  var countCommits : Long = 0

  def isEmpty: Boolean = !TURN_ON || fileList.isEmpty

  def contains(filename: String): Boolean = fileList.contains(filename)

  def buildFilter(projectName: String): Unit = {
    if (fileList.nonEmpty) { fileList.clear() }

    val unit: Try[Unit] = Using(Source.fromFile(FILEPATH)) {
      source => {
        val it = source.getLines()

        while (it.hasNext) {
          val content = it.next

          if (content.split(",").head.strip.toLowerCase == projectName.toLowerCase) {
            val info = content.split(",").apply(1).strip
            fileList.add(info.split("/").last.strip)
          }
        }
      }
    }

    if (unit.isFailure) {
      println("Error to process filter.csv file")
    }
  }
}
