package xyz.pubps.app.sink

import com.mongodb.client.model.DeleteOneModel
import com.mongodb.client.model.DeleteOptions
import com.mongodb.client.model.Filters
import com.mongodb.client.model.ReplaceOneModel
import com.mongodb.client.model.ReplaceOptions
import com.mongodb.client.model.WriteModel
import dto.KafkaMessage
import org.apache.flink.connector.mongodb.sink.writer.context.MongoSinkContext
import org.apache.flink.connector.mongodb.sink.writer.serializer.MongoSerializationSchema
import org.bson.BsonDocument
import org.bson.Document
import xyz.pubps.app.objectMapper

class CustomMongoSerializer: MongoSerializationSchema<KafkaMessage> {

  override fun serialize(
    element: KafkaMessage,
    sinkContext: MongoSinkContext
  ): WriteModel<BsonDocument> {

    val op = element.value["op"]

    if (op == "d") {
      return DeleteOneModel(
        Filters.eq<String>("_id",
          objectMapper.writeValueAsString(element.key)),
        DeleteOptions()
      )
    }

    val replacement = Document()
    val after = element.value["after"] as Map<*, *>
    after.forEach {
      replacement.append(it.key as String, it.value)
    }
    return ReplaceOneModel(
      Filters.eq<String>("_id", objectMapper.writeValueAsString(element.key)),
      replacement.toBsonDocument(),
      ReplaceOptions().upsert(true)
    )
  }
}