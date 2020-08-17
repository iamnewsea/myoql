@file:JvmName("MyHelper")
@file:JvmMultifileClass

package nbcp.comm

import nbcp.utils.MyUtil
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*


/**类型转换
 * 把数据转为  clazz 类型。
 */
fun Any.ConvertType(clazz: Class<*>): Any? {
    var theClass = this.javaClass;
    if (clazz.isAssignableFrom(theClass)) {
        return this;
    }

//    var className = clazz.name;

    if (clazz == Boolean::class.java || clazz == java.lang.Boolean::class.java) {
        return this.AsBooleanWithNull()
    }
    if (clazz == Character::class.java || clazz == Char::class.java) {
        return this.toString()[0]
    }
    if (clazz == Byte::class.java || clazz == java.lang.Byte::class.java) {
        return this.AsInt().toByte()
    }
    if (clazz == Short::class.java || clazz == java.lang.Short::class.java) {
        return this.AsInt().toShort()
    }
    if (clazz == Int::class.java ||
            clazz == java.lang.Integer::class.java) {
        return this.AsInt()
    }
    if (clazz == Long::class.java || clazz == java.lang.Long::class.java) {
        return this.AsLong()
    }
    if (clazz == Float::class.java || clazz == java.lang.Float::class.java) {
        return this.AsFloat()
    }
    if (clazz == Double::class.java || clazz == java.lang.Double::class.java) {
        return this.AsDouble()
    }
    if (CharSequence::class.java.isAssignableFrom(clazz) || clazz == java.lang.String::class.java) {
        return this.AsString()
    }

    //可以 Collection Array 互转
    if (clazz.isArray) {
        if (this is Collection<*>) {
            var ret = java.lang.reflect.Array.newInstance(clazz.componentType, this.size) as Array<*>
            this.forEachIndexed { index, it ->
                java.lang.reflect.Array.set(ret, index, if (it == null) null else it.ConvertType(clazz.componentType));
            }
            return ret;
        }
    }

    if (theClass.isArray) {
        //因为 set 也是 Collection,所以先转 set
        if (clazz is Set<*>) {
            return (this as Array<*>).toMutableSet();
        } else if (clazz.IsCollectionType()) {
            return (this as Array<*>).toMutableList();
        }
    }

    if (clazz.isEnum) {
        if (this is Number) {
            return this.AsInt().ToEnum(clazz)
        } else if (this is String) {
            return this.toString().ToEnum(clazz)
        } else {
            return null;
        }
    }

    if (clazz == LocalDate::class.java) {
        return this.AsLocalDate()
    }
    if (clazz == LocalTime::class.java) {
        return this.AsLocalTime()
    }
    if (clazz == LocalDateTime::class.java) {
        return this.AsLocalDateTime()
    }
    if (clazz == Date::class.java) {
        var dt = this.AsLocalDateTime();
        if (dt == null) return dt;
        return Date.from(dt.atZone(ZoneId.systemDefault()).toInstant());
    }

    return this.ConvertJson(clazz);
}


/**
 * @param format 时间格式化字符串，数字的话也可以传 0.0,#.#
 */
fun Any?.AsString(defaultValue: String = ""): String {
    if (this == null) return defaultValue;
    if (this is String) {
        if (this.isEmpty()) return defaultValue
        return this;
    } else if (this is CharArray) {
        return String(this)
    } else if (this is LocalDateTime) {
        if (this == LocalDateTime.MIN) {
            return "";
        }
        if (this.hour == 0 && this.minute == 0 && this.second == 0) {
            return this.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }
        return this.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    } else if (this is LocalDate) {
        if (this == LocalDate.MIN) {
            return "";
        }
        return this.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    } else if (this is LocalTime) {
        if (this == LocalTime.MIN) {
            return "";
        }
        return this.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
    } else if (this is Date) {
        if (this.time == 0L) {
            return "";
        }
        if( this.time % (MyUtil.OneDaySeconds *1000) == 0L){
            return SimpleDateFormat("yyyy-MM-dd").format(this);
        }
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(this);
    } else if (this is Number) {

        return this.toString();
    }

    var ret = this.toString()
    if (ret.isEmpty()) return defaultValue
    return ret
}


fun Any?.AsBoolean(defaultValue: Boolean = false): Boolean {
    return this.AsBooleanWithNull() ?: defaultValue
}

fun Any?.AsBooleanWithNull(): Boolean? {
    if (this == null) return null;
    if (this is Boolean) return this;

    if (this is Number) {
        if (this == 1) return true;
        if (this == 0) return false;

        return null;
    }

    if (this is Char) {
        if (this == '1') return true;
        if (this == '0') return false
        return null
    }

    var value = this;

    if (value is CharSequence) {
        value = value.toString()
    }


    if (value is String) {
        if (value.equals("true", true)) return true
        if (value.equals("yes", true)) return true
        if (value.equals("1", true)) return true

        if (value.equals("false", true)) return false
        if (value.equals("no", true)) return false
        if (value.equals("0", true)) return false
    }
    return null;
}


