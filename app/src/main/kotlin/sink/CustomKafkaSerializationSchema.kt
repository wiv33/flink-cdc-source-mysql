package sink

import dto.KafkaMessage
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema
import org.apache.kafka.clients.producer.ProducerRecord
import xyz.pubps.app.objectMapper
import java.time.Instant

class CustomKafkaSerializationSchema : KafkaRecordSerializationSchema<KafkaMessage> {

  override fun serialize(
    element: KafkaMessage,
    context: KafkaRecordSerializationSchema.KafkaSinkContext,
    timestamp: Long?
  ): ProducerRecord<ByteArray, ByteArray> {

    // example: mysql_source.my_database.my_table -> my_table
    val extractTableName = element.topic.split(Regex("\\.")).last()
    return ProducerRecord(
      extractTableName,
      null,
      Instant.now().toEpochMilli(),
      objectMapper.writeValueAsBytes(element.key),
      objectMapper.writeValueAsBytes(element.value)
    )

  }
}