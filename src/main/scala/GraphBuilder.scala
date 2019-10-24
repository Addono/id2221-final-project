import org.apache.spark.sql.SparkSession
import org.graphframes.GraphFrame

/**
 * Retrives event data from the Github Archive and builds a GraphFrames graph from this.
 * The vertices are usernames and repository names, edges are events originating from
 * users towards the repository they act on.
 */
object GraphBuilder {
  def main(args: Array[String]) {

    // Define our Spark session
    val spark = SparkSession.builder
      .appName("Github GraphFrame Builder")
        // Master not selected, as this is set by the Google Cloud Spark environment.
//      .master("spark://localhost:7077")
      .getOrCreate()

    // Load our input data
    val data = spark.read.json("gs://data.gharchive.org/%s.json.gz".format(args(0)))

    // Print the schema
    data.printSchema()

    // Define the columns we are going to use.
    val actors = data.col("actor.login")
    val repositories = data.col("repo.name")
    val actions = data.col("type")

    // Define the edges and vertices as DFs
    val e = data.select(
      actors.alias("src"),
      repositories.alias("dst"),
      actions.alias("action")
    ).distinct()
    val v = data.select(actors.alias("id")).union(data.select(repositories.alias("id"))).distinct()

    // Construct the graph
    val graph = GraphFrame(v, e)

    // Store the graph
    graph.vertices.write.parquet("%s/vertices".format(args(1)))
    graph.edges.write.parquet("%s/edges".format(args(1)))

    spark.stop()
  }
}
