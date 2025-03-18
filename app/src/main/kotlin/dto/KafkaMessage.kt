package dto

import java.io.Serializable

data class KafkaMessage(
  val topic: String,
  val key: Map<String, Any?>,
  val value: Map<String, Any?>,
  val sourcePartition: Map<String, Any?>,
  val sourceOffset: Map<String, Any?>
) : Serializable, Cloneable {
}
