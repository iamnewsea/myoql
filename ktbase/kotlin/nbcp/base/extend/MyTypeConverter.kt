@file:JvmName("MyHelper")
@file:JvmMultifileClass

package nbcp.base.extend

import nbcp.base.utils.MyUtil
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.*


/**类型转换
 * 把数据转为  clazz 类型。
 * @param targetClass 目标类
 * @param genericClassIfTargetIsList 如果目标类是 List 泛型类， 泛型类类型。
 */
@JvmOverloads
fun Any.ConvertType(targetClass: Class<*>, genericClassIfTargetIsList: Class<*>? = null): Any? {
    var theClass = this.javaClass;

    if (!targetClass.IsCollectionType && targetClass.isAssignableFrom(theClass)) {
        return this;
    }

//    var className = clazz.name;
    if (targetClass == Character::class.java || targetClass == Char::class.java) {
        return this.toString()[0]
    }

    if (CharSequence::class.java.isAssignableFrom(targetClass) || targetClass == java.lang.String::class.java) {
        return this.AsString()
    }

    //可以 Collection Array 互转
    if (targetClass.isArray) {
        if (this is Collection<*>) {
            var ret = java.lang.reflect.Array.newInstance(targetClass.componentType, this.size) as Array<*>

            this.forEachIndexed { index, it ->
                java.lang.reflect.Array.set(
                    ret,
                    index,
                    if (it == null) null else it.ConvertType(targetClass.componentType)
                );
            }
            return ret;
        } else if (this is Array<*>) {
            var ret = java.lang.reflect.Array.newInstance(targetClass.componentType, this.size) as Array<*>

            this.forEachIndexed { index, it ->
                java.lang.reflect.Array.set(
                    ret,
                    index,
                    if (it == null) null else it.ConvertType(targetClass.componentType)
                );
            }
            return ret;
        }
    } else if (targetClass.IsCollectionType) {
        if (theClass.isArray) {
            (this as Array<*>)
                .map {
                    if (genericClassIfTargetIsList == null || it == null) return@map it;
                    return@map it.ConvertType(genericClassIfTargetIsList)
                }
                .apply {
                    if (targetClass.isAssignableFrom(Set::class.java)) {
                        return this.toMutableSet()
                    } else {
                        return this.toMutableList();
                    }
                }

        } else if (theClass.IsCollectionType) {
            (this as Collection<*>)
                .map {
                    if (genericClassIfTargetIsList == null || it == null) return@map it;
                    return@map it.ConvertType(genericClassIfTargetIsList)
                }
                .apply {
                    if (targetClass.isAssignableFrom(Set::class.java)) {
                        return this.toMutableSet()
                    } else {
                        return this.toMutableList();
                    }
                }
        }
    } else if (targetClass.isEnum) {
        if (this is Number) {
            return this.AsInt().ToEnum(targetClass)
        } else if (this is String) {
            return this.toString().ToEnum(targetClass)
        } else {
            return null;
        }
    }

    if (targetClass == Boolean::class.java || targetClass == java.lang.Boolean::class.java) {
        return this.AsBooleanWithNull()
    }

    if (targetClass == Byte::class.java || targetClass == java.lang.Byte::class.java) {
        return this.AsInt().toByte()
    }
    if (targetClass == Short::class.java || targetClass == java.lang.Short::class.java) {
        return this.AsInt().toShort()
    }
    if (targetClass == Int::class.java ||
        targetClass == java.lang.Integer::class.java
    ) {
        return this.AsInt()
    }
    if (targetClass == Long::class.java || targetClass == java.lang.Long::class.java) {
        return this.AsLong()
    }
    if (targetClass == Float::class.java || targetClass == java.lang.Float::class.java) {
        return this.AsFloat()
    }
    if (targetClass == Double::class.java || targetClass == java.lang.Double::class.java) {
        return this.AsDouble()
    }

    if (targetClass == LocalDate::class.java) {
        return this.AsLocalDate()
    }
    if (targetClass == LocalTime::class.java) {
        return this.AsLocalTime()
    }
    if (targetClass == LocalDateTime::class.java) {
        return this.AsLocalDateTime()
    }
    if (targetClass == Date::class.java) {
        var dt = this.AsLocalDateTime();
        if (dt == null) return dt;
        return Date.from(dt.atZone(ZoneId.systemDefault()).toInstant());
    }

    return this.ConvertJson(targetClass);
}

@JvmOverloads
fun Any?.AsString(defaultValue: String = ""): String {
    var ret = this.AsStringWithNull();
    if (ret.isNullOrEmpty()) return defaultValue;
    return ret;
}


fun Any?.AsString(defaultValueCallback: () -> String): String {
    var ret = this.AsStringWithNull();
    if (ret.isNullOrEmpty()) return defaultValueCallback();
    return ret;
}

/**
 */
fun Any?.AsStringWithNull(): String? {
    if (this == null) return null;
    if (this is String) {
        if (this.isEmpty()) return null
        return this;
    } else if (this is CharArray) {
        return String(this)
    } else if (this is LocalDateTime) {
        if (this == LocalDateTime.MIN) {
            return null;
        }
        if (this.hour == 0 && this.minute == 0 && this.second == 0) {
            return this.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }
        return this.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    } else if (this is LocalDate) {
        if (this == LocalDate.MIN) {
            return null;
        }
        return this.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    } else if (this is LocalTime) {
        if (this == LocalTime.MIN) {
            return null;
        }
        return this.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
    } else if (this is Date) {
        if (this.time == 0L) {
            return null;
        }
        if (this.time % (MyUtil.OneDaySeconds * 1000) == 0L) {
            return SimpleDateFormat("yyyy-MM-dd").format(this);
        }
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(this);
    } else if (this is Number) {
        //数字使用格式化函数 format
        return this.toString();
    }

    return this.toString()
}


@JvmOverloads
fun Any?.AsBoolean(defaultValue: Boolean = false): Boolean {
    return this.AsBooleanWithNull() ?: defaultValue
}

fun Any?.AsBooleanWithNull(): Boolean? {
    if (this == null) return null;
    if (this is Boolean) return this;

    if (this is Number) {
        if (this.toDouble() == 1.0) return true;
        if (this.toDouble() == 0.0) return false;

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
        if (value.equals("enable", true)) return true

        if (value.equals("false", true)) return false
        if (value.equals("no", true)) return false
        if (value.equals("0", true)) return false
        if (value.equals("disable", true)) return false

    }
    return null;
}


