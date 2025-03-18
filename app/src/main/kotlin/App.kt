package xyz.pubps.app

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import config.VircleFlinkConfig
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import sink.MongoSinkV2
import sink.VircleKafkaSink
import source.VircleMySqlSource
import xyz.pubps.app.dto.TableTransformer.Companion.existsTransform

val objectMapper: ObjectMapper = JsonMapper.builder()
  .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
  .build()

fun main() {
  val env = VircleFlinkConfig().getEnv()

  // source Mysql
  val mySqlSource = VircleMySqlSource().build()

  val cdcSourceStream = env.fromSource(
    mySqlSource,
    WatermarkStrategy.noWatermarks(),
    "CDC MySQL Source"
  ).setParallelism(1)
    .map {
      existsTransform(it.topic, it)
    }

  cdcSourceStream.print()

  // sink kafka
  val kafkaSink = VircleKafkaSink().build()
  cdcSourceStream
    .rebalance()
    .sinkTo(kafkaSink)
    .uid("from-cdc-mysql-to-kafka")


  if (System.getenv("MONGO_HOST") != null) {
    println("RUN mongo sink")
    // sink mongo
    val mongoSink = MongoSinkV2();
    cdcSourceStream
      .rebalance()
      .sinkTo(mongoSink)
      .uid("from-cdc-mysql-to-mongodb")

  }

  env.execute("From MySQL to other storage")
}
