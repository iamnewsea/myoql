package nbcp.db.sql

import nbcp.comm.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*
import java.sql.Types;

/**
 * Created by yuxh on 2018/11/9
 */

//站在开发角度,泛泛的数据类型.
public enum class DbType {
    String("字符串", kotlin.String::class.java, Types.VARCHAR),
    Enum("枚举", kotlin.String::class.java, Types.VARCHAR),
    Int("整数", kotlin.Int::class.java, Types.INTEGER, java.lang.Integer::class.java),
    Float("浮点数", kotlin.Float::class.java, Types.FLOAT, java.lang.Float::class.java),
    Long("长整数", kotlin.Long::class.java, Types.BIGINT, java.lang.Long::class.java),
    Double("双精度", kotlin.Double::class.java, Types.DOUBLE, java.lang.Double::class.java),
    Byte("字节", kotlin.Byte::class.java, Types.TINYINT, java.lang.Byte::class.java),
    Short("短整数", kotlin.Short::class.java, Types.SMALLINT, java.lang.Short::class.java),
    Boolean("布尔", kotlin.Boolean::class.java, Types.BIT, java.lang.Boolean::class.java),
    Decimal("大数字", BigDecimal::class.java, Types.DECIMAL),
    Date("日期", LocalDate::class.java, Types.DATE),
    Time("时间", LocalTime::class.java, Types.TIME),
    DateTime("日期时间", LocalDateTime::class.java, Types.TIMESTAMP),
    Binary("二进制数据", ByteArray::class.java, Types.VARBINARY),

    Text("大文本", kotlin.String::class.java, Types.LONGVARCHAR),
    Json("Json", kotlin.String::class.java, Types.LONGVARCHAR),

    //比如sql表达式.
    Other("表达式类型", Any::class.java, Types.OTHER);


    var remark: kotlin.String = "";
    var javaType: Class<*>;
    var javaRefType: Class<*>?;
    var sqlType: kotlin.Int

    constructor(remark: kotlin.String, javaType: Class<*>, sqlType: kotlin.Int, javaRefType: Class<*>? = null) {
        this.remark = remark;
        this.javaType = javaType;
        this.sqlType = sqlType;
        this.javaRefType = javaRefType;
    }

    //是否是Text类型. 需要 单引号包裹
    //Other 是复合列。
    fun needTextWrap(): kotlin.Boolean {
        return this == DbType.String || this == DbType.Text || this == DbType.Enum || this.isDateOrTime();//|| this == DbType.Other;
    }

    //是否是数字格式.
    fun isNumberic(): kotlin.Boolean {
        return isInteger() || this == DbType.Float || this == DbType.Double;
    }

    fun isDateOrTime(): kotlin.Boolean {
        return this == DbType.Date || this == DbType.Time || this == DbType.DateTime;
    }

    //是否是整数
    fun isInteger(): kotlin.Boolean {
        return this == DbType.Int || this == DbType.Long || this == DbType.Short || this == DbType.Byte || this == DbType.Boolean
    }

    /**
     * 代码生成器用。
     */
    fun toMySqlTypeString(): kotlin.String {
        return when (this) {
            String -> "varchar(800)"
            Text -> "text(65535)"
            Enum -> "varchar(800)"
            Int -> "int"
            Float -> "float"
            Long -> "bigint"
            Double -> "double"
            Byte, Short -> "tinyint"
            Boolean -> "bit"
            Decimal -> "decimal"
            Date -> "date"
            Time -> "time"
            DateTime -> "datetime"
            Binary -> "binary"
            Json -> "JSON"
            Other -> ""
        }
    }

    /**
     * 代码生成器用。
     */
    fun toKotlinType(): kotlin.String {
        if (this == DbType.Boolean) {
            return "Boolean?"
        }
        if (this == DbType.Date) {
            return "LocalDate?"
        }
        if (this == DbType.Time) {
            return "LocalTime?"
        }
        if (this == DbType.DateTime) {
            return "LocalDateTime?"
        }
        if (this == DbType.Other) {
            return "Any?"
        }
        if (this == DbType.Text || this == DbType.Enum || this == DbType.Json) {
            return "String"
        }
        return this.javaType.kotlinTypeName;
    }

    /**
     * 代码生成器用
     */
    fun toKotlinDefaultValue(): kotlin.String {
        return when (this) {
            String, Text, Json, Enum -> "\"\""
            Float -> "0F"
            Long -> "0L"
            Double -> "0.0"
            Int, Byte, Short -> "0"
            Decimal -> "BigDecimal.ZERO"
            Boolean, Date, Time, DateTime, Other -> "null"
            Binary -> "byteArrayOf()"
            else -> "null"
        }
    }

    companion object {
        @JvmStatic
        fun <T> of(clazz: Class<T>): DbType {

            if (clazz.isEnum) {
                return DbType.Enum;
            }

            DbType.values()
                    .firstOrNull { it.javaType == clazz || (it.javaRefType != null && it.javaRefType == clazz) }
                    .apply {
                        if (this != null) {
                            return this;
                        }
                    }

            if (clazz == Character::class.java || clazz == java.lang.Character::class.java) {
                return DbType.String
            }

            if (clazz == Date::class.java) {
                return DbType.DateTime
            }

            return DbType.Other
        }
    }
}