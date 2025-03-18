package sink

import dto.KafkaMessage
import org.apache.flink.connector.base.DeliveryGuarantee
import org.apache.flink.connector.mongodb.sink.MongoSink
import xyz.pubps.app.sink.CustomMongoSerializationSchema

@Deprecated("Use the class MongoSinkV2 in sink package. Not supports dynamic collection or database")
class VircleMongoSink {
  val mongoUri: String = System.getenv("MONGO_HOST")
  val mongoDatabase: String = System.getenv("MONGO_DATABASE")
  fun build(): MongoSink<KafkaMessage> {
    return MongoSink.builder<KafkaMessage>()
      .setDeliveryGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
      .setUri(mongoUri)
      .setDatabase(mongoDatabase)
      .setCollection("joined_customers_flink")
      .setBatchSize(100)
      .setBatchIntervalMs(1000)
      .setSerializationSchema(CustomMongoSerializationSchema())
      .build()

  }
}