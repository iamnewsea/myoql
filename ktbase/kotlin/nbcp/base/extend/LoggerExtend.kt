@file:JvmName("MyHelper")
@file:JvmMultifileClass

package nbcp.base.extend

import nbcp.base.enums.LogLevelScopeEnum
import nbcp.base.utils.MyUtil
import nbcp.base.utils.ReflectUtil
import org.slf4j.Logger
import java.io.File


/**
 * inline 内联方式可以拿到调用栈信息
 * 该方法会忽略 LogLevel，使用 isInfoEnabled，isErrorEnabled 先进行判断是否记录日志
 */
inline fun Logger.InfoError(error: Boolean, msgFunc: (() -> String)) {
    if (error && this.scopeErrorLevel) {
        var msg = msgFunc();
        if (msg.isEmpty()) return;
        this.error(msg)
    } else if (this.scopeInfoLevel) {
        var msg = msgFunc();
        if (msg.isEmpty()) return;
        this.info(msg)
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

inline fun Logger.Error(err: Throwable) {
    this.error(err.message, err);
}

/**
 * 重要日志，使用 scope.info 记录
 */
inline fun Logger.Important(msg: String) {
    usingScope(LogLevelScopeEnum.IMPORTANT) {
        this.warn(msg);
    }
}

fun ch.qos.logback.classic.Logger.getLoggerFile(configName: String): String {
    var appenderList = this.iteratorForAppenders();
    if (appenderList.hasNext()) {
        var fileAppender =
            (appenderList.Filter { it.name == configName }.first() as ch.qos.logback.core.rolling.RollingFileAppender)
        return (ReflectUtil.getValueByWbsPath(fileAppender, "currentlyActiveFile") as File).absolutePath
    }

    var parent = ReflectUtil.getValueByWbsPath(this, "parent") as ch.qos.logback.classic.Logger?
    if (parent == null) return "";
    return parent.getLoggerFile(configName);
}
//
//fun LogLevel.toLevel(): Level {
//    when (this) {
//        LogLevel.INFO -> return Level.INFO
//        LogLevel.DEBUG -> return Level.DEBUG
//        LogLevel.ERROR -> return Level.ERROR
//        LogLevel.FATAL -> return Level.ERROR
//        LogLevel.OFF -> return Level.OFF
//        LogLevel.TRACE -> return Level.TRACE
//        LogLevel.WARN -> return Level.WARN
//
//        else -> return Level.INFO
//    }
//}
//
//fun Level.toLogLevel(): LogLevel {
//    when (this) {
//        Level.INFO -> return LogLevel.INFO
//        Level.DEBUG -> return LogLevel.DEBUG
//        Level.ERROR -> return LogLevel.ERROR
//
//        Level.OFF -> return LogLevel.OFF
//        Level.TRACE -> return LogLevel.TRACE
//        Level.WARN -> return LogLevel.WARN
//
//        else -> return LogLevel.INFO
//    }
//}
