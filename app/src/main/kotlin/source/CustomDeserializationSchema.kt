package source

import com.ververica.cdc.debezium.DebeziumDeserializationSchema
import dto.KafkaMessage
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.util.Collector
import org.apache.kafka.connect.data.Struct
import org.apache.kafka.connect.source.SourceRecord
import java.time.Instant

fun intToDateTime(epochDays: Int): Instant {
  return Instant.ofEpochSecond(epochDays * 86400L)
}

class CustomDeserializationSchema : DebeziumDeserializationSchema<KafkaMessage> {

  override fun deserialize(record: SourceRecord, out: Collector<KafkaMessage>) {
    val keyStruct = record.key() as? Struct
    val valueStruct = record.value() as? Struct
    val topic = record.topic() as String

    val keyStructMap = structToMap(keyStruct)
    val valueStructMap = structToMap(valueStruct)

    out.collect(
      KafkaMessage(
        topic, keyStructMap, valueStructMap,
        record.sourcePartition(), record.sourceOffset()
      )
    )
  }

  override fun getProducedType(): TypeInformation<KafkaMessage> {
    return TypeInformation.of(KafkaMessage::class.java) as TypeInformation<KafkaMessage>
  }

  private fun structToMap(struct: Struct?): Map<String, Any?> {
    // Struct -> Map 타입 transform
    return struct?.schema()?.fields()?.associate { field ->
      val value = struct[field]
      field.name() to if (value is Struct) {
        structToMap(value)
      } else if (field.schema()?.name()?.contains("Date") == true && value is Int) {
        val resultValue = intToDateTime(value.toString().toInt())
        resultValue
      } else value
    } ?: emptyMap()
  }

}