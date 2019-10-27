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

    val graph: GraphFrame = constructGraph(args(0), spark)

    storeGraph(args(1), graph)

    spark.stop()
  }

  def constructGraph(inputMatch: String, spark: SparkSession): GraphFrame = {
    // Load our input data
    val data = spark.read.json("gs://data.gharchive.org/%s.json.gz".format(inputMatch))

    // Print the schema
    data.printSchema()

    // Define the columns we are going to use.
    val actors = data.col("actor.login")
    val repositories = data.col("repo.name")
    val actions = data.col("type")

    // Define the edges and vertices as DFs
    val e = data.select(actors.alias("src"), repositories.alias("dst"), actions.alias("action")).distinct()
    val v = data.select(actors.alias("id")).union(data.select(repositories.alias("id"))).distinct()

    // Construct the graph
    GraphFrame(v, e)
  }

  def storeGraph(directory: String, graph: GraphFrame): Unit = {
    graph.vertices.repartition(1).write.csv("%s/vertices".format(directory))
    graph.edges.repartition(1).write.csv("%s/edges".format(directory))
  }

}
