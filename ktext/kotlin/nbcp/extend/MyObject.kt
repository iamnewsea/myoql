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
 * Created by udi on 17-4-3.
 */

data class CheckMustExpresstion<T>(var condition: Boolean, var data: T?) {

    fun elseThrow(msg: String): T {
        if (condition) return data!!
        else throw RuntimeException(msg);
    }
}

fun <T> T?.must(trueCondition: ((T?) -> Boolean)): CheckMustExpresstion<T> {
    return CheckMustExpresstion(trueCondition(this), this)
}

fun <T> T.IsIn(vararg values: T): Boolean {
    return this.IsIn(null, *values);
}

fun <T> T.IsIn(equalFunc: ((T, T) -> Boolean)? = null, vararg values: T): Boolean {
    for (i in values) {
        if (equalFunc == null) {
            if (this == i) return true;
        } else {
            if (equalFunc(this, i)) return true;
        }
    }

    if (values.size == 1 && values[0] is Collection<*>) {
        for (i in values[0] as Collection<*>) {
            if (equalFunc == null) {
                if (this == i) return true;
            } else {
                if (equalFunc(this, i as T)) return true;
            }
        }
    }

    return false;
}

/**
 * 大于等于 start 并且 小于等于 end
 */
fun <T : Comparable<in T>> T.Between(start: T?, end: T?): Boolean {
    if (start == null || end == null) return false;
    if (this.compareTo(start) < 0) return false;
    if (this.compareTo(end) > 0) return false;
    return true;
}

/**
 * 查找最近添加的。
 * @param enumValues: 如果有值，则精确查找该值进行返回。
 */
inline fun <reified R> Stack<*>.getLatestScope(vararg enumValues: R): R? {
    if (this.size == 0) return null

    for (i in this.indices.reversed()) {
        var item = this[i];
        if (item is R) {
            if (enumValues.isEmpty() || enumValues.contains(item)) {
                return item;
            } else {
                continue;
            }
        }
    }


    return null;
}

fun Serializable.ToSerializableByteArray(): ByteArray {
    ByteArrayOutputStream().use { byteOutStream ->
        ObjectOutputStream(byteOutStream).use { objStream ->
            objStream.writeObject(this);
            objStream.flush();

            return byteOutStream.toByteArray();
        }
    }
}

fun ByteArray.ToSerializableObject(): Serializable {
    ByteArrayInputStream(this).use { byteInStream ->
        ObjectInputStream(byteInStream).use { objStream ->
            return objStream.readObject() as Serializable
        }
    }
}



fun Temporal.BetweenSeconds(nextTime: Temporal): Int {
    return Duration.between(this.AsLocalDateTime(), nextTime.AsLocalDateTime()).getSeconds().AsInt();
}

fun Temporal.BetweenDays(nextTime: Temporal): Int {
    return (Duration.between(this.AsLocalDateTime(), nextTime.AsLocalDateTime()).getSeconds() / MyUtil.OneDaySeconds).AsInt();
}


fun Logger.Error(err: Throwable) {
    this.error(err.message, err);
}

//返回非空的描述
val Throwable.Detail: String
    get() = this.message.AsString(this::class.java.simpleName)



//通过内存复制对象.
fun <T : Serializable> T.CloneObject(): T {
    var obj = this;
    //写入字节流
    var out = ByteArrayOutputStream()
    ObjectOutputStream(out).use { obs ->
        obs.writeObject(obj);
    }


    //分配内存，写入原始对象，生成新对象
    var ios = ByteArrayInputStream(out.toByteArray());
    ObjectInputStream(ios).use { ois ->
        //返回生成的新对象
        var cloneObj = ois.readObject() as T;
        return cloneObj;
    }
}

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


/**
 * 输入16进制内容。
 */
fun ByteArray.ToHexLowerString(): String {
    return this.map { it.toString(16) }.joinToString("")
}


fun ch.qos.logback.classic.Logger.getLoggerFile(configName: String): String {
    var appenderList = this.iteratorForAppenders();
    if (appenderList.hasNext()) {
        var fileAppender = (appenderList.Filter { it.name == configName }.first() as ch.qos.logback.core.rolling.RollingFileAppender)
        return (MyUtil.getPrivatePropertyValue(fileAppender, "currentlyActiveFile") as File).absolutePath
    }

    var parent = MyUtil.getPrivatePropertyValue(this, "parent") as ch.qos.logback.classic.Logger?
    if (parent == null) return "";
    return parent.getLoggerFile(configName);
}
