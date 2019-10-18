import org.apache.spark.sql.SparkSession;

object GraphBuilder {
  def main(args: Array[String]) {
    val logFile = "build.sbt" // Should be some file on your system

    val spark = SparkSession.builder
      .appName("Simple Application")
      .master("spark://localhost:7077")
      .getOrCreate()

    val sum = spark.range(20).select("id").agg("id" -> "sum")
    sum.collect().foreach { println }

    spark.stop()
  }
}
