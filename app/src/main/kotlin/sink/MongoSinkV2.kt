package sink

import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import com.mongodb.client.model.ReplaceOptions
import dto.KafkaMessage
import org.apache.flink.api.common.eventtime.Watermark
import org.apache.flink.api.connector.sink2.Sink
import org.apache.flink.api.connector.sink2.SinkWriter
import org.apache.flink.api.connector.sink2.SinkWriter.Context
import org.bson.Document

// Flink SinkV2 MongoDB Sink
class MongoSinkV2 : Sink<KafkaMessage> {
  val mongoUri: String = System.getenv("MONGO_HOST")
  val mongoDatabase: String = System.getenv("MONGO_DATABASE")

  @Deprecated("Deprecated in Java")
  override fun createWriter(context: Sink.InitContext): SinkWriter<KafkaMessage> {
    return MongoSinkWriter(mongoUri, mongoDatabase)
  }

  private class MongoSinkWriter(
    mongoUri: String,
    databaseName: String
  ) : SinkWriter<KafkaMessage> {

    private val database: MongoDatabase = MongoClients.create(mongoUri).getDatabase(databaseName)
    override fun writeWatermark(watermark: Watermark?) {
      super.writeWatermark(watermark)
    }

    override fun write(record: KafkaMessage, context: Context) {
      val collectionName = record.topic.split(Regex("\\.")).last()
      val collection: MongoCollection<Document> = database.getCollection(collectionName)

      val op = record.value["op"]

      when (op) {
        "d" -> {
          collection.deleteOne(
            Filters.eq<Map<*, *>>(
              "_id", record.key
            )
          )
          return
        }

        else -> {
          val document = Document()
          (record.value["after"] as Map<*, *>)
            .forEach { (key, fieldValue) -> document.append(key.toString(), fieldValue) }

          val updateResult = collection.replaceOne(
            Filters.eq<Map<*, *>>("_id", record.key),
            document,
            ReplaceOptions().upsert(true)
          )

          if (!updateResult.wasAcknowledged()) {
            println("Failure write to mongo : $record")
          }
        }
      }
    }

    override fun flush(p0: Boolean) {
      // noting
    }

    override fun close() {
      // MongoDB 드라이버는 별도 close 필요 없음 (Connection Pool 사용)
    }
  }
}
