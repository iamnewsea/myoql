package nbcp.comm

import ch.qos.logback.classic.Level
import org.springframework.boot.logging.LogLevel
import java.io.Closeable


/**
 * value = Level.toLevel识别的参数，不区分大小写，如：all|trace|debug|info|warn|error|off
 */
enum class LogScope() : Closeable {
    all,
    trace,
    debug,
    info,
    warn,
    error,
    off;


    override fun close() {
    }
}


enum class OrmLogScope(val remark: String) : Closeable {
    IgnoreExecuteTime("不记录执行时间"),
    IgnoreAffectRow("不记录影响行数");

    override fun close() {
    }
}



