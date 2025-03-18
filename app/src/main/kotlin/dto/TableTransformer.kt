package xyz.pubps.app.dto

import com.fasterxml.jackson.core.type.TypeReference
import dto.KafkaMessage
import xyz.pubps.app.objectMapper
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.function.Function

enum class TableTransformer(
  val code: String,
  val transform: Function<KafkaMessage, KafkaMessage>
) {
  TB_CUSTOMER_DASHBOARD_STATISTICS(
    "TB_CUSTOMER_DASHBOARD_STATISTICS",
    Function { message ->
      // 변환 로직
      val value = message.value.toMap(mutableMapOf<String, Any?>())

      val resultValue = if (value["op"] == "d") {
        val before = value["before"] as MutableMap<String, Any>
        val data = objectMapper.readValue<MutableMap<String, Any>>(
          before["data"] as String,
          object : TypeReference<MutableMap<String, Any>>() {})

        before.put("data", data)
        value.put("before", before)

        value
      } else {
        val after = value["after"] as MutableMap<String, Any>
        val data = objectMapper.readValue<MutableMap<String, Any>>(
          after["data"] as String,
          object : TypeReference<MutableMap<String, Any>>() {})

        after.put("data", data)
        value.put("after", after)

        value
      }

      message.copy(value = resultValue)
    }),
  ;

  companion object {
    fun existsTransform(tableName: String, message: KafkaMessage): KafkaMessage {
      return entries.find { tableName.uppercase().contains(it.code) }?.transform?.apply(message)
        ?: message
    }
  }

}