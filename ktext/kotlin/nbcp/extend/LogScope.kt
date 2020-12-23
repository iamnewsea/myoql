package nbcp.comm

import ch.qos.logback.classic.Level
import org.springframework.boot.logging.LogLevel
import java.io.Closeable


/**
 * value = ch.qos.logback.classic.Level.级别
 * TRACE < DEBUG < INFO < WARN < ERROR
 */
class LogScope(val level: Int) : Closeable {
    companion object {
        /**
         * 记录所有的Info
         */
        fun ImportantInfo(): LogScope {
            return LogScope(Level.INFO_INT);
        }

        /**
         * 记录所有记录
         */
        fun AllTrace(): LogScope {
            return LogScope(Level.TRACE_INT);
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



