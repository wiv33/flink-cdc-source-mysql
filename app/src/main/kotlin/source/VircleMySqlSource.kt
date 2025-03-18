package source

import com.ververica.cdc.connectors.mysql.source.MySqlSource
import com.ververica.cdc.connectors.mysql.table.StartupOptions
import dto.KafkaMessage


class VircleMySqlSource {
  val rdbHost = System.getenv("RDB_HOST")!!
  val rdbPort = System.getenv("RDB_PORT")!!
  val rdbUsername = System.getenv("RDB_USERNAME")!!
  val rdbPassword = System.getenv("RDB_PASSWORD")!!
  val rdbDatabaseList = System.getenv("RDB_DATABASE_LIST")!!
  val rdbTableList = System.getenv("RDB_TABLE_LIST")!!

  fun build(): MySqlSource<KafkaMessage> {
    return MySqlSource.builder<KafkaMessage>()
      .hostname(rdbHost)
      .port(rdbPort.toInt())
      .username(rdbUsername)
      .password(rdbPassword)
      .databaseList(rdbDatabaseList)  // 대상 데이터베이스
      .tableList(rdbTableList)  // 대상 테이블 (와일드카드 가능: "my_database.*")
      .startupOptions(StartupOptions.latest())  // 시작 시점 설정 (initial, latest, timestamp 등)
      .deserializer(CustomDeserializationSchema()) // JSON 형태로 데이터 처리
      .serverTimeZone("Asia/Seoul")
      .build()
  }
}