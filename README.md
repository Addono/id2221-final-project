# ID2221 - Final Project

## Table of Contents
+ [About](#about)
+ [Getting Started](#getting_started)
+ [Usage](#usage)
+ [Resources](#resources)

## About <a name = "about"></a>
This project leverages the [Github Archive](https://gharchive.org) dataset. 

At the moment of writing this it is 789 GiB of compressed data:
```bash
$ gsutil du -sh gs://data.gharchive.org
789.57 GiB   gs://data.gharchive.org
```

## Getting Started <a name = "getting_started"></a>
<!--These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. See [deployment](#deployment) for notes on how to deploy the project on a live system.-->

### Prerequisites

* [scala](https://scala-lang.org/download/)
* [sbt](https://www.scala-sbt.org/download.html)
* [Google Cloud SDK](https://cloud.google.com/sdk/) (optional, alternatively use the [web console](https://console.cloud.google.com))


### Installing
Clone the repository.

Use SBT to download the 

## Usage <a name = "usage"></a>

First, we are going to build the application, make sure to use [sbt-assembly](https://github.com/sbt/sbt-assembly) to ensure that the dependencies are added to the JAR:
```bash
sbt assembly
```

[Create a DataProc cluster](https://cloud.google.com/dataproc/docs/guides/create-cluster) if you don't have one running yet.

Launch the project on a DataProc cluster, make sure to update the name of the cluster, the clusters region (if not set to global), the input selector and output location.
 
**The input selector should not match files created before 1th of January 2015, so for example `201*-01-01-10` is illegal.**
```bash
gcloud dataproc jobs submit spark --jars target/scala-2.11/github-graphframe-builder-assembly-0.2.jar --cluster gh-archive-dataproc --region europe-west1 --class GraphBuilder -- "2015-01-01-*" gs://gh-grahpframes/2015-01-01
```

```bash
gcloud dataproc jobs submit spark --jars target/scala-2.11/github-graphframe-builder-assembly-0.2.jar --cluster gh-archive-dataproc --region europe-west1  --class LabelPropegationRunner -- gs://gh-graphframes/2015-01-01 5
```

### Cleanup
To prevent unnecessary costs, make sure to destroy all resources which you aren't using anymore.

First, the most expensive thing to keep running is probably going to be the DataProc cluster. Destroy it by running:
```bash
gcloud dataproc clusters delete gh-archive-dataproc
``` 

Also, along the way we have stored some files into Cloud Storage, e.g. the JAR we assembled or parquet files as job artifacts (see the second runtime argument):
```bash
# Individual files
gsutil rm gs://<bucket-name>/myfilename.txt

# Directories
gsutil rm -r gs://<bucket-name>/
```

## Resources <a name = "resources"></a>
* [Write and run Spark Scala jobs on Cloud Dataproc](https://cloud.google.com/dataproc/docs/tutorials/spark-scala)
* [Github Archive](https://www.gharchive.org)
* [GraphFrames Documentation](https://graphframes.github.io/graphframes/docs/_site/index.html)
