package sink

import dto.KafkaMessage
import org.apache.flink.connector.base.DeliveryGuarantee
import org.apache.flink.connector.kafka.sink.KafkaSink
import org.apache.kafka.clients.producer.ProducerConfig
import java.util.*


class VircleKafkaSink {
  val kafkaHost: String = System.getenv("KAFKA_HOST")
  val kafkaApiKey: String = System.getenv("KAFKA_API_KEY")
  val kafkaApiSecret: String = System.getenv("KAFKA_API_SECRET")

  fun build(): KafkaSink<KafkaMessage> {
    val txId = "cdc-mysql-source-tx"

    val kafkaSink = KafkaSink.builder<KafkaMessage>()
      .setBootstrapServers(kafkaHost)
      .setKafkaProducerConfig(Properties().apply {
        // for confluent cloud
        put("sasl.mechanism", "PLAIN")
        put("security.protocol", "SASL_SSL")
        put(
          "sasl.jaas.config",
          "org.apache.kafka.common.security.plain.PlainLoginModule required username='${kafkaApiKey}' password='${kafkaApiSecret}';"
        )
        put(ProducerConfig.CLIENT_DNS_LOOKUP_CONFIG, "use_all_dns_ips")

        // exactly once
        put(ProducerConfig.TRANSACTION_TIMEOUT_CONFIG, 900_000)
        put(ProducerConfig.ACKS_CONFIG, "all")
        put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true")
        put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5)
        put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, txId)
      })
      .setDeliveryGuarantee(DeliveryGuarantee.EXACTLY_ONCE)
      .setRecordSerializer(CustomKafkaSerializationSchema())

    return kafkaSink.build()
  }
}