@file:JvmName("MyHelper")
@file:JvmMultifileClass

package nbcp.base.extend

import com.google.common.cache.Cache
import nbcp.base.comm.JsonMap
import nbcp.base.utils.MyUtil
import org.slf4j.LoggerFactory
import org.springframework.util.AntPathMatcher
import java.io.*
import java.time.Duration
import java.time.temporal.Temporal

/**
 * Created by udi on 17-4-3.
 */

data class CheckMustExpression<T>(var condition: Boolean, var data: T?) {
    fun elseThrow(msg: ((T?) -> String)): T {
        if (condition) return data!!
        else throw RuntimeException(msg(data));
    }

    inline fun elseReturn(block: T?.() -> Unit): T {
        if (condition) return data!!
        data.block()
        throw RuntimeException("else Return 必须使用 return!")
    }
}




@JvmOverloads
fun <T> T?.must(trueCondition: ((T?) -> Boolean)? = null): CheckMustExpression<T> {
    return CheckMustExpression(if (trueCondition == null) this != null else trueCondition(this), this)
}

fun <T> Boolean.ifTrue(trueAction: (() -> T)): T? {
    if (!this) return null;
    return trueAction();
}

fun <T> Boolean.ifFalse(falseAction: (() -> T)): T? {
    if (this) return null;
    return falseAction();
}

fun <T> T.IsIn(vararg values: T): Boolean {
    return this.IsIn(null, *values);
}

fun <T> T.IsIn(equalFunc: ((T, T) -> Boolean)?, vararg values: T): Boolean {
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
fun <T : Comparable<in T>> T.IfRangeTo(start: T?, end: T?): Boolean {
    if (start == null || end == null) return false;
    if (this < start) return false;
    if (this > end) return false;
    return true;
}


/**
 * 大于等于 且 小于
 */
fun <T : Comparable<in T>> T.IfUntil(start: T?, end: T?): Boolean {
    if (start == null || end == null) return false;
    if (this < start) return false;
    if (this >= end) return false;
    return true;
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


/**
 * 开闭区间
 */
fun Temporal.RangeToSeconds(nextTime: Temporal): Int {
    return Duration.between(this.AsLocalDateTime(), nextTime.AsLocalDateTime()).getSeconds().AsInt();
}


/**
 * 开闭区间
 */
fun Temporal.RangeToDays(nextTime: Temporal): Int {
    return (Duration.between(this.AsLocalDateTime(), nextTime.AsLocalDateTime())
            .getSeconds() / MyUtil.OneDaySeconds).AsInt();
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
        var cloneObj = ois.readObject();
        return cloneObj as T;
    }
}


/**
 * 输入16进制内容。
 */
fun ByteArray.ToHexLowerString(): String {
    return this.map { it.toString(16) }.joinToString("")
}

//fun <T : Any> T.Iif(conditionValue: T, retValue: T): T {
//    if (this == conditionValue) return retValue
//    return conditionValue
//}


/*
比较两个数组的内容是否相同, 去除相同数据进行比较 .如:
[1,1,2] .equalArrayContent( [1,2,2] )  == true
 */
//fun Array<*>.EqualArrayContent(other: Array<*>, withIndex: Boolean = false): Boolean {
//    return this.toList().EqualArrayContent(other.toList(), withIndex);
//}

/*
比较两个数组的内容是否相同, 去除相同数据进行比较 .如:
[1,1,2] .equalArrayContent( [1,2,2] )  == true
 */
@JvmOverloads
fun Collection<*>.EqualArrayContent(other: Collection<*>, withIndex: Boolean = false): Boolean {
    if (this.size == 0 && other.size == 0) return true;
    else if (this.size == 0) return false;
    else if (other.size == 0) return false;

    if (withIndex) {
        this.forEachIndexed { index, item ->
            var otherItem = other.elementAt(index);
            if (item basicSame otherItem == false) {
                return false;
            }
        }
        return true;
    }

    var one = this.distinct();
    var two = other.distinct();


    if (one.size != two.size) return false;
    return one.intersect(two).size == this.size;
}


/**
 * 基本相等，不区分大小写格式的比较，listOf() basicSame null 。
 * "abc" basicSame "aBc" is true
 */
infix fun Any?.basicSame(other: Any?): Boolean {
    if (this == other) {
        return true;
    }

    if (this == null) {
        if (other == null) {
            return true;
        }

        if (other is Collection<*>) {
            if (other.size == 0) {
                return true;
            }
        } else if (other is Array<*>) {
            if (other.size == 0) {
                return true;
            }
        } else if (other is Map<*, *>) {
            if (other.size == 0) {
                return true;
            }
        }

    } else if (this is Collection<*>) {
        if (this.size == 0 && other == null) {
            return true;
        }

        if (other is Collection<*>) {
            return this.EqualArrayContent(other)
        } else if (other is Array<*>) {
            return this.EqualArrayContent(other.toList())
        }
    } else if (this is Array<*>) {
        if (this.size == 0 && other == null) {
            return true;
        }

        if (other is Collection<*>) {
            return this.toList().EqualArrayContent(other)
        } else if (other is Array<*>) {
            return this.toList().EqualArrayContent(other.toList())
        }
    } else if (this is Map<*, *>) {
        if (this.size == 0 && other == null) {
            return true;
        }

        if (other is Map<*, *>) {
            return this.EqualMapContent(other)
        }
    } else {
        if (this.AsString().compareTo(other.AsString(), true) == 0) {
            return true;
        }
    }

    return false;
}

fun Any?.simpleFieldToJson(
        initLevel: Int = 1, maxLength: Int = 256
): Any? {
    return this.simpleFieldToValue(initLevel, initLevel, maxLength);
}

/**
 * 仅对简单字段转为Map
 */
private fun Any?.simpleFieldToValue(initLevel: Int, level: Int, maxLength: Int): Any? {
    if (this == null) return null;


    var type = this::class.java;
    if (type.IsSimpleType()) {
        if (this is String) {
            if (this.length > maxLength) {
                return this.Slice(0, maxLength) + " ..."
            }
        }
        return this;
    } else if (type.isArray) {
        return (this as Array<Any?>).map { it.simpleFieldToValue(initLevel, level, maxLength) }
                .filter { it != null && it != "~" }
    } else if (type.IsCollectionType) {
        return (this as Collection<Any?>).map { it.simpleFieldToValue(initLevel, level, maxLength) }
                .filter { it != null && it != "~" }
    }


    if (level <= 0) {
        return "~";
    }
    if (type.IsMapType) {
        return (this as Map<String, Any?>)
                .mapValues { it.value.simpleFieldToValue(initLevel, level - 1, maxLength) }
                .filter { it.value != null }
    }


    var map = JsonMap();
    type.AllFields.forEach {
        val v = it.get(this).simpleFieldToValue(initLevel, level - 1, maxLength);
        if (v == null) {
            return@forEach
        }

        map.put(it.name, v)
    }

    if (initLevel != level) {
        if (map.keys.any() == false) {
            return null;
        }
        return map;
    }

    try {
        return map.ToJson();
    } catch (ex: Exception) {
        LoggerFactory.getLogger("myoql.simpleFieldToJson").error(ex.message, ex)
        return "(err)"
    }
}