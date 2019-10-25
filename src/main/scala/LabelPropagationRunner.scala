import org.apache.spark.sql.SparkSession
import org.graphframes.GraphFrame

object LabelPropagationRunner {
  def main(args: Array[String]) {
    // Define our Spark session
    val spark = SparkSession.builder
      .appName("Github GraphFrame Builder")
      .config("spark.sql.autoBroadcastJoinThreshold", 10485760*1000) // 10MB * 1000 = 10GB
      .config("spark.sql.broadcastTimeout", -1) // Indefinite
      .getOrCreate()

    // Load our input data
    val v = spark.read.parquet("%s/vertices".format(args(0)))
      .distinct()
      .persist()
    val e = spark.read.parquet("%s/edges".format(args(0)))
      .distinct()
      .persist()

    // Construct the graph
    val graph = GraphFrame(v, e)

    // Run label propagation algorithm
    val iterations = if (args.length >= 2) args(1).toInt else 5
    val result = graph.labelPropagation.maxIter(iterations).run().persist()

    v.unpersist()
    e.unpersist()

    // Process output
    result.printSchema()
    val output = result.select("id", "label").persist()

    output.show()
    output.write.parquet("%s/%s_%s".format(args(0), java.time.LocalDateTime.now, iterations.toString))

    spark.stop()
  }
}
