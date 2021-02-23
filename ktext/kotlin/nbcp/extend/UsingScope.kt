@file:JvmName("MyHelper")
@file:JvmMultifileClass

package nbcp.comm

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

class ScopeStack : Stack<Any>() {}

private val _scopes = ThreadLocal.withInitial { ScopeStack() }

val scopes: ScopeStack
    get() = _scopes.get();

/**
 * 在作用域中添加 String 类型的值。
 * usingScope(StringScopeData("key","value")){
 *  scopes.getLatestStringScope("key")
 * }
 */
data class StringScopeData(var key: String, var value: String)

/**
 * 用法:
 *
 * @param initObjects: 可以是 array, list,AutoCloseable,any, 如果是 array,list，则依次添加到作用域栈中。
 */
inline fun <T, M : Any> usingScope(initObjects: M, body: () -> T): T {
    return usingScope(initObjects, body, {})
}


inline fun <T, M : Any> usingScope(initObjects: M, body: () -> T, finally: ((M) -> Unit)): T {
    var init_list = mutableListOf<Any>()

    if (initObjects is Collection<*>) {
        init_list.addAll(initObjects as Collection<Any>)
    } else if (initObjects is Array<*>) {
        init_list.addAll(initObjects.map { it!! })
    } else {
        init_list.add(initObjects)
    }

    init_list.forEach {
        scopes.push(it);
    }

    try {
        var ret = body();

        init_list.asReversed().forEach {
            if (it is AutoCloseable) {
                it.close()
            }
        }
        finally(initObjects)
        return ret;
    } finally {
        for (i in 0 until init_list.size) {
            if (scopes.isEmpty() == false) {
                scopes.pop()
            }
        }
    }
}


/**
 * 按类型获取当前域 ,  互斥枚举类型：枚举有 mutexGroup:String 属性。
 */
inline fun <reified R> ScopeStack.getScopeTypes(): Set<R> {
    if (this.size == 0) return setOf()

    var list = mutableSetOf<R>()
    for (i in this.indices.reversed()) {
        var item = this[i];
        if (item is R) {
            list.add(item);
        }
    }


    var retType = R::class.java;
    if (retType.isEnum) {
        var mutexGroupField = retType.GetEnumStringField()
        if (mutexGroupField != null && mutexGroupField.name == "mutexGroup") {
            var groups = mutableSetOf<String>()
            var removeItems = mutableSetOf<R>()
            for (i in list.indices) {
                var item = list.elementAt(i);
                var group = mutexGroupField.get(item).toString();
                if (groups.contains(group)) {
                    removeItems.add(item);
                } else {
                    groups.add(group)
                }
            }

            list.removeAll(removeItems);
        }
    }
    return list;
}

/**
 * 获取指定 key 为 String 的 value
 * usingScope(StringScopeData("key","value")){
 *   scopes.getLatestStringScope("key")
 * }
 */
fun ScopeStack.getLatestStringScope(key: String): String {
    if (this.size == 0) return ""
    if (key.isEmpty()) return ""

    for (i in this.indices.reversed()) {
        var item = this[i];
        if (item is StringScopeData) {
            if (item.key == key) {
                return item.value;
            } else {
                continue;
            }
        }
    }

    return "";
}

inline val Logger.scopeInfoLevel: Boolean
    get() {
        var logs = scopes.getScopeTypes<LogScope>()
        if (logs.any()) {
            return logs.any { ch.qos.logback.classic.Level.INFO_INT >= ch.qos.logback.classic.Level.toLevel(it.name).levelInt }
        }

        return this.isInfoEnabled;
    }

inline val Logger.scopeErrorLevel: Boolean
    get() {
        var logs = scopes.getScopeTypes<LogScope>()
        if (logs.any()) {
            return logs.any { ch.qos.logback.classic.Level.ERROR_INT >= ch.qos.logback.classic.Level.toLevel(it.name).levelInt }
        }

        return this.isErrorEnabled;
    }
