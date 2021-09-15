@file:JvmName("MyHelper")
@file:JvmMultifileClass

package nbcp.comm

import nbcp.comm.*
import nbcp.utils.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.*
import java.time.temporal.Temporal
import java.io.*
import java.lang.RuntimeException
import java.util.*


/**
 * inline 内联方式可以拿到调用栈信息
 * 该方法会忽略 LogScope，使用 isInfoEnabled，isErrorEnabled 先进行判断是否记录日志
 */
inline fun Logger.InfoError(error: Boolean, msgFunc: (() -> String)) {
    if (this.scopeInfoLevel) {
        var msg = msgFunc();
        if (msg.isEmpty()) return;
        this.info(msg)
    } else if (error && this.scopeErrorLevel) {
        var msg = msgFunc();
        if (msg.isEmpty()) return;
        this.error(msg)
    }
}


inline fun Logger.Info(msgFunc: (() -> String)) {
    if (this.scopeInfoLevel) {
        this.info(msgFunc())
    }
}

inline fun Logger.Error(msgFunc: (() -> String)) {
    if (this.scopeErrorLevel) {
        this.error(msgFunc())
    }
}

fun Logger.Error(err: Throwable) {
    this.error(err.message, err);
}

/**
 * 重要日志，使用 scope.info 记录
 */
fun Logger.Important(msg: String) {
    usingScope(LogScope.info) {
        this.info(msg);
    }
}

fun ch.qos.logback.classic.Logger.getLoggerFile(configName: String): String {
    var appenderList = this.iteratorForAppenders();
    if (appenderList.hasNext()) {
        var fileAppender =
            (appenderList.Filter { it.name == configName }.first() as ch.qos.logback.core.rolling.RollingFileAppender)
        return (MyUtil.getPrivatePropertyValue(fileAppender, "currentlyActiveFile") as File).absolutePath
    }

    var parent = MyUtil.getPrivatePropertyValue(this, "parent") as ch.qos.logback.classic.Logger?
    if (parent == null) return "";
    return parent.getLoggerFile(configName);
}
