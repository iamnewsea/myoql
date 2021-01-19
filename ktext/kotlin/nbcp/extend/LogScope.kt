package nbcp.comm

import ch.qos.logback.classic.Level
import org.springframework.boot.logging.LogLevel
import java.io.Closeable


/**
 * value = Level.toLevel识别的参数，不区分大小写，如：all|trace|debug|info|error|off
 */
class LogScope(val level: String) : Closeable {
    companion object {
        /**
         * 记录所有的Info
         */
        fun ImportantInfo(): LogScope {
            return LogScope("info");
        }

        /**
         * 记录所有记录
         */
        fun AllTrace(): LogScope {
            return LogScope("all");
        }
    }

    override fun close() {
    }
}


enum class OrmLogScope(val remark: String) : Closeable {
    IgnoreExecuteTime("不记录执行时间"),
    IgnoreAffectRow("不记录影响行数");

    override fun close() {
    }
}



