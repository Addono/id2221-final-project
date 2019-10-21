name := "github-graphframe-builder"

version := "0.1"

scalaVersion := "2.11.12"

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) =>
    (xs map {_.toLowerCase}) match {
      case ("manifest.mf" :: Nil) | ("index.list" :: Nil) | ("dependencies" :: Nil) =>
        MergeStrategy.discard
      case ps @ (x :: xs) if ps.last.endsWith(".sf") || ps.last.endsWith(".dsa") =>
        MergeStrategy.discard
      case "plexus" :: xs =>
        MergeStrategy.discard
      case "services" :: xs =>
        MergeStrategy.filterDistinctLines
      case ("spring.schemas" :: Nil) | ("spring.handlers" :: Nil) =>
        MergeStrategy.filterDistinctLines
      case _ => MergeStrategy.first
    }
    case _ =>  MergeStrategy.first
}

// Add Spark Packages repository, as GraphFrames is not available in the default repository.
resolvers += Resolver.bintrayRepo("spark-packages", "maven")

libraryDependencies += "org.apache.spark" %% "spark-sql" % "2.4.4" %  "provided"

libraryDependencies += "com.google.cloud.bigdataoss" % "gcs-connector" % "hadoop2-2.0.0" % "provided"

// https://mvnrepository.com/artifact/graphframes/graphframes
libraryDependencies += "graphframes" % "graphframes" % "0.7.0-spark2.4-s_2.11"

// https://stackoverflow.com/a/51792292/7500339
dependencyOverrides += "com.google.guava" % "guava" % "15.0" % "provided"
