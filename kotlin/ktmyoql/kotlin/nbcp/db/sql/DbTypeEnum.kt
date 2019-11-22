package nbcp.db.sql

import nbcp.base.extend.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

/**
 * Created by yuxh on 2018/11/9
 */

//站在开发角度,泛泛的数据类型.
public enum class DbType {
    String(kotlin.String::class.java),
    Enum(kotlin.String::class.java),
    Int(kotlin.Int::class.java),
    Float(kotlin.Float::class.java),
    Long(kotlin.Long::class.java),
    Double(kotlin.Double::class.java),
    Byte(kotlin.Byte::class.java),
    Short(kotlin.Short::class.java),
    Boolean(kotlin.Boolean::class.java),
    Date(LocalDate::class.java),
    Time(LocalTime::class.java),
    DateTime(LocalDateTime::class.java),
    Binary(ByteArray::class.java),

    //比如sql表达式.
    Other(Any::class.java);


    var javaType: Class<*>;

    constructor(parameterJavaType: Class<*>) {
        this.javaType = parameterJavaType;
    }

    //是否是Text类型. 需要 单引号包裹
    //Other 是复合列。
    fun needTextWrap(): kotlin.Boolean {
        return this == DbType.String || this == DbType.Enum || this.isDateOrTime() || this == DbType.Other;
    }

    //是否是数字格式.
    fun isNumberic(): kotlin.Boolean {
        return isInteger() || isDecimal();
    }

    fun isDateOrTime(): kotlin.Boolean {
        return this == DbType.Date || this == DbType.Time || this == DbType.DateTime;
    }

    //是否是整数
    fun isInteger(): kotlin.Boolean {
        return this == DbType.Int || this == DbType.Long || this == DbType.Short || this == DbType.Byte || this == DbType.Boolean
    }

    //是否是 小数
    fun isDecimal(): kotlin.Boolean {
        return this == DbType.Float || this == DbType.Double
    }

    companion object {
        fun <T> of(clazz: Class<T>): DbType {
            var className = clazz.name;
            if (clazz == kotlin.Boolean::class.java || className == "java.lang.Boolean") {
                return DbType.Boolean
            }

            if (clazz == Byte::class.java || className == "java.lang.Byte") {
                return DbType.Byte;
            }
            if (clazz == Short::class.java || className == "java.lang.Short") {
                return DbType.Short;
            }
            if (clazz == kotlin.Int::class.java ||
                    clazz == java.lang.Integer::class.java) {
                return DbType.Int;
            }

            if (clazz == Long::class.java || className == "java.lang.Long") {
                return DbType.Int
            }

            if (clazz == kotlin.Float::class.java || className == "java.lang.Float") {
                return DbType.Float
            }
            if (clazz == Double::class.java || className == "java.lang.Double") {
                return DbType.Double
            }

            if (clazz.isEnum) {
                return DbType.Enum;
            }

            if (clazz == Character::class.java || className == "Char::class.java" ||
                    clazz == kotlin.String::class.java) {
                return DbType.String
            }

            if (clazz == Date::class.java ||
                    clazz == LocalDateTime::class.java) {
                return DbType.DateTime
            }

            if (clazz == LocalDate::class.java) {
                return DbType.Date
            }
            if (clazz == LocalTime::class.java) {
                return DbType.Time
            }
            return DbType.Other
        }
    }
}