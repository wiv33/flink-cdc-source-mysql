package config

import org.apache.flink.api.common.RuntimeExecutionMode
import org.apache.flink.configuration.CheckpointingOptions
import org.apache.flink.configuration.Configuration
import org.apache.flink.configuration.StateBackendOptions
import org.apache.flink.core.execution.CheckpointingMode
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment


class VircleFlinkConfig {
  val parallelism = (System.getenv("FLINK_PARALLELISM") ?: "1").toInt()
  val flinkEnv = System.getenv("FLINK_ENV") ?: "local"
  val checkpointRootDir = System.getenv("CHECKPOINT_ROOT_DIR") ?: "s3://vircle-flink/cdc/source-mysql/v1"
  fun getEnv(): StreamExecutionEnvironment {
    val env = StreamExecutionEnvironment.getExecutionEnvironment()

    env.parallelism = parallelism
    env.setRuntimeMode(RuntimeExecutionMode.STREAMING)
    env.enableCheckpointing(10_000) // 10초마다 체크포인트 생성
    env.checkpointConfig.checkpointingConsistencyMode = CheckpointingMode.EXACTLY_ONCE
    env.checkpointConfig.minPauseBetweenCheckpoints = 2000
    env.checkpointConfig.checkpointTimeout = 60_000
    env.checkpointConfig.maxConcurrentCheckpoints = parallelism

    // 아래 설정은 불필요할 수도 있음.
    val s3PathPrefix = "$checkpointRootDir/${flinkEnv}"
    env.setDefaultSavepointDirectory("$s3PathPrefix/savepoints")
    val config = Configuration()
    config.set(StateBackendOptions.STATE_BACKEND, "rocksdb");
    config.set(CheckpointingOptions.CHECKPOINT_STORAGE, "filesystem");
    config.set(CheckpointingOptions.CHECKPOINTS_DIRECTORY, "$s3PathPrefix/checkpoints")
    config.set(CheckpointingOptions.SAVEPOINT_DIRECTORY, "$s3PathPrefix/savepoints")
    config.set(CheckpointingOptions.ENABLE_CHECKPOINTS_AFTER_TASKS_FINISH, true)
    config.setString("state.savepoints.dir", "$s3PathPrefix/savepoints")
    config.setString("savepoints.state.backend.fs.dir", "$s3PathPrefix/savepoints")
    env.configure(config)
    return env
  }
}