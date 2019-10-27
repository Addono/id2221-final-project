import org.apache.spark.sql.{SaveMode, SparkSession}

object LabelPropagationRunner {
  def main(args: Array[String]) {
    val outputBucketName = args(0) + "/csv"
    val batches = args.slice(1, args.length)

    batches.foreach { batch => {
      // Define our Spark session
      val spark = SparkSession.builder
        .appName("Github GraphFrame Builder")
        .config("spark.sql.autoBroadcastJoinThreshold", 10485760*1000) // 10MB * 1000 = 10GB
        .config("spark.sql.broadcastTimeout", -1) // Indefinite
        .getOrCreate()

      // Get the graph
      val graph = GraphBuilder.constructGraph(batch, spark)

      // Store the graph
      GraphBuilder.storeGraph(outputBucketName + "/" + batch, graph)

      // Run label propagation algorithm
      val result = graph.labelPropagation.maxIter(5).run()

      // Process output
      result.printSchema()
      val output = result.select("id", "label")

      output.show()
      output.repartition(1).write.mode(SaveMode.Overwrite).csv("%s/%s/label_propagation/%s".format(outputBucketName, batch, 5.toString))

      spark.stop()
    }}
  }
}
