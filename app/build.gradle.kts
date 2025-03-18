plugins {
  // Apply the shared build logic from a convention plugin.
  // The shared code is located in `buildSrc/src/main/kotlin/kotlin-jvm.gradle.kts`.
  id("buildsrc.convention.kotlin-jvm")
  id("com.github.johnrengelman.shadow") version "8.1.1"
  // Apply the Application plugin to add support for building an executable JVM application.
  application
}
val flinkVersion = "1.20.1"
dependencies {
  // Project "app" depends on project "utils". (Project paths are separated with ":", so ":utils" refers to the top-level "utils" project.)
  implementation(project(":utils"))
  implementation("org.apache.kafka:kafka-clients:3.9.0")

  implementation("org.eclipse.jetty:jetty-http:12.0.17")
  implementation("org.bitbucket.b_c:jose4j:0.9.6")
  implementation("com.google.guava:guava:33.4.0-jre")
  implementation("com.ververica:flink-connector-debezium:3.0.1")
  implementation("com.mysql:mysql-connector-j:9.2.0")
  implementation("com.ververica:flink-connector-mysql-cdc:3.0.1")

  implementation("org.apache.flink:flink-clients:${flinkVersion}")
  implementation("org.apache.flink:flink-runtime-web:${flinkVersion}")
  implementation("org.apache.flink:flink-connector-base:${flinkVersion}")
  implementation("org.apache.flink:flink-streaming-java:${flinkVersion}")
  implementation("org.apache.flink:flink-table-api-java:${flinkVersion}")
  implementation("org.apache.flink:flink-statebackend-rocksdb:${flinkVersion}")
  implementation("org.apache.flink:flink-s3-fs-presto:${flinkVersion}")
  implementation("org.apache.flink:flink-json:${flinkVersion}")

  implementation("org.apache.flink:flink-shaded-jackson:2.18.2-20.0")

  // logging
  implementation("org.slf4j:slf4j-api:2.0.17")
  implementation("org.slf4j:slf4j-simple:2.0.17")
//  implementation("ch.qos.logback:logback-classic:1.5.17")
//  implementation("ch.qos.logback:logback-core:1.5.17")

  implementation("org.apache.flink:flink-connector-kafka:3.4.0-1.20")
  // mongodb
  implementation("org.apache.flink:flink-connector-mongodb:1.2.0-1.19")
}

application {
  // Define the Fully Qualified Name for the application main class
  // (Note that Kotlin compiles `App.kt` to a class with FQN `com.example.app.AppKt`.)
  mainClass = "xyz.pubps.app.AppKt"
}


tasks.shadowJar {
  archiveBaseName.set("cdc-mysql-to-kafka-and-mongo-app")
  archiveClassifier.set("")
  archiveVersion.set("1.0.0")

  isZip64 = true

  // 중복 및 불필요한 파일 제거
  exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
//  exclude("org/apache/logging/**") // 예: 로깅 라이브러리 제외
}