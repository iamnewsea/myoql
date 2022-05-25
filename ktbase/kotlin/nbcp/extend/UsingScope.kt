@file:JvmName("MyHelper")
@file:JvmMultifileClass

package nbcp.comm

import ch.qos.logback.classic.Level
import nbcp.scope.*
import org.slf4j.Logger
import org.springframework.boot.logging.LogLevel
import java.io.Flushable
import java.util.*


private val _scopes = ThreadLocal.withInitial { ScopeStack() }

val scopes: ScopeStack
    get() = _scopes.get();


/**
 * 用法:
 *
 * @param initObjects: 可以是 array, list,AutoCloseable,any, 如果是 array,list，则依次添加到作用域栈中。
 */
inline fun <T> usingScope(initObjects: List<IScopeData>, body: () -> T): T {
    return usingScope(initObjects, body, {})
}


inline fun <T> usingScope(initObjects: Array<out IScopeData>, body: () -> T): T {
    return usingScope(initObjects.toList(), body, {})
}


inline fun <T> usingScope(initObject: IScopeData, body: () -> T): T {
    return usingScope(listOf(initObject), body, {})
}

inline fun <T> usingScope(initObject: IScopeData, body: () -> T, finally: ((IScopeData) -> Unit)): T {
    return usingScope(listOf(initObject), body, finally)
}

inline fun <T> usingScope(init_list: List<out IScopeData>, body: () -> T, finally: ((IScopeData) -> Unit)): T {
//    if (initObjects is Level) {
//        throw RuntimeException("请使用 LogLevel 枚举")
//    }

    init_list.forEach {
        scopes.push(it);
    }

    try {
        var ret = body();

        //自动释放
        init_list.asReversed().forEach {
            if (it is Flushable) {
                it.flush()
            }
            if (it is AutoCloseable) {
                it.close()
            }

            finally(it)
        }

        return ret;
    } finally {
        //自动释放
        init_list.asReversed().forEach {
            scopes.pop();
        }
    }
}


inline val Logger.scopeInfoLevel: Boolean
    get() {
        val logs = scopes.getScopeTypes<LogLevelScope>()
        if (logs.any()) {
            return logs.any { Level.INFO_INT >= it.value }
        }

        return this.isInfoEnabled;
    }

inline val Logger.scopeErrorLevel: Boolean
    get() {
        val logs = scopes.getScopeTypes<LogLevelScope>()
        if (logs.any()) {
            return logs.any { Level.ERROR_INT >= it.value }
        }

        return this.isErrorEnabled;
    }
