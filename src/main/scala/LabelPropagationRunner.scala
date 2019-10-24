import org.apache.spark.sql.SparkSession
import org.graphframes.GraphFrame

object LabelPropagationRunner {
  def main(args: Array[String]) {
    // Define our Spark session
    val spark = SparkSession.builder
      .appName("Github GraphFrame Builder")
      .getOrCreate()

    // Load our input data
    val v = spark.read.parquet("%s/vertices".format(args(0))).distinct().persist()
    val e = spark.read.parquet("%s/edges".format(args(0))).distinct().persist()

    // Construct the graph
    val graph = GraphFrame(v, e)

    // Run label propagation algorithm
    val iterations = if (args.length >= 2) args(1).toInt else 5
    val result = graph.labelPropagation.maxIter(iterations).run()

    // Process output
    result.select("id", "label").show()

    spark.stop()
  }
}
