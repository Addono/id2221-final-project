import org.apache.spark.sql.SparkSession
import org.graphframes.GraphFrame

object GraphBuilder {
  def main(args: Array[String]) {

    // Define our Spark session
    val spark = SparkSession.builder
      .appName("Github GraphFrame Builder")
        // Master not selected, as this is set by the Google Cloud Spark environment.
//      .master("spark://localhost:7077")
//      .master("local[*]")
      .getOrCreate()

    // Load our input data
    val data = spark.read.json("gs://data.gharchive.org/2015-01-02-*.json.gz")

    // Define the columsn we are going to use.
    val actors = data.col("actor.login")
    val repositories = data.col("repo.name")
    val actions = data.col("type")

    // Define the edges and vertices as DFs
    val e = data.select(
      actors.alias("src"),
      repositories.alias("dst"),
      actions.alias("action")
    )
    val v = data.select(actors.alias("id")).union(data.select(repositories.alias("id")))

    // Construct the graph
    val graph = GraphFrame(v, e)

    // Store the graph
    graph.vertices.write.parquet("gs://gh-graphframes/2015-01-02/vertices")
    graph.edges.write.parquet("gs://gh-graphframes/2015-01-02/edges")

    spark.stop()
  }
}
