package nbcp.myoql.db.sql.enums

import nbcp.base.extend.AsInt
import nbcp.base.extend.IsCollectionType
import nbcp.base.extend.kotlinTypeName
import java.math.BigDecimal
import java.sql.Types
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.reflect.KClass

/**
 * Created by yuxh on 2018/11/9
 */

//站在开发角度,泛泛的数据类型.
public enum class DbType {
    STRING("字符串", kotlin.String::class.java, Types.VARCHAR),
    ENUM("枚举", kotlin.String::class.java, Types.VARCHAR),
    SET("枚举", kotlin.String::class.java, Types.VARCHAR),
    INT("整数", kotlin.Int::class.java, Types.INTEGER, java.lang.Integer::class.java),
    FLOAT("浮点数", kotlin.Float::class.java, Types.FLOAT, java.lang.Float::class.java),
    LONG("长整数", kotlin.Long::class.java, Types.BIGINT, java.lang.Long::class.java),
    DOUBLE("双精度", kotlin.Double::class.java, Types.DOUBLE, java.lang.Double::class.java),
    BYTE("字节", kotlin.Byte::class.java, Types.TINYINT, java.lang.Byte::class.java),
    SHORT("短整数", kotlin.Short::class.java, Types.SMALLINT, java.lang.Short::class.java),
    BOOLEAN("布尔", kotlin.Boolean::class.java, Types.BIT, java.lang.Boolean::class.java),
    DECIMAL("大数字", BigDecimal::class.java, Types.DECIMAL),
    DATE("日期", LocalDate::class.java, Types.DATE),
    TIME("时间", LocalTime::class.java, Types.TIME),
    DATE_TIME("日期时间", LocalDateTime::class.java, Types.TIMESTAMP),
    BINARY("二进制数据", ByteArray::class.java, Types.VARBINARY),

    TEXT("大文本", kotlin.String::class.java, Types.LONGVARCHAR),
    JSON("Json", kotlin.String::class.java, Types.LONGVARCHAR),

    //比如sql表达式.
    OTHER("表达式类型", Any::class.java, Types.OTHER);


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
        return this == STRING || this == TEXT || this == ENUM || this == SET || this.isDateOrTime();//|| this == DbType.Other;
    }

    //是否是数字格式.
    fun isNumberic(): kotlin.Boolean {
        return isInteger() || this == FLOAT || this == DOUBLE;
    }

    fun isDateOrTime(): kotlin.Boolean {
        return this == DATE || this == TIME || this == DATE_TIME;
    }

    //是否是整数
    fun isInteger(): kotlin.Boolean {
        return this == INT || this == LONG || this == SHORT || this == BYTE || this == BOOLEAN
    }

    /**
     * 代码生成器用。
     */
    fun toMySqlTypeString(varcharLength: kotlin.Int = 0, enumItems: kotlin.String = ""): kotlin.String {
        return when (this) {
            STRING -> "varchar(${varcharLength.AsInt(200)})"
            TEXT -> "text(65535)"
            ENUM -> "Enum(${enumItems})"
            SET -> "Set(${enumItems})"
            INT -> "int"
            FLOAT -> "float"
            LONG -> "bigint"
            DOUBLE -> "double"
            BYTE, SHORT -> "tinyint"
            BOOLEAN -> "bit"
            DECIMAL -> "decimal"
            DATE -> "date"
            TIME -> "time"
            DATE_TIME -> "datetime"
            BINARY -> "binary"
            JSON -> "JSON"
            OTHER -> ""
        }
    }

    /**
     * 代码生成器用。
     */
    fun toKotlinType(): kotlin.String {
        if (this == BOOLEAN) {
            return "Boolean?"
        }
        if (this == DATE) {
            return "LocalDate?"
        }
        if (this == TIME) {
            return "LocalTime?"
        }
        if (this == DATE_TIME) {
            return "LocalDateTime?"
        }
        if (this == OTHER || this == JSON) {
            return "Any?"
        }

        if (this == TEXT || this == ENUM) {
            return "String"
        }

        if (this == SET) {
            return "Set?"
        }
        return this.javaType.kotlinTypeName;
    }

    /**
     * 代码生成器用
     */
    fun toKotlinDefaultValue(): kotlin.String {
        return when (this) {
            STRING, TEXT, ENUM -> "\"\""
            FLOAT -> "0F"
            LONG -> "0L"
            DOUBLE -> "0.0"
            INT, BYTE, SHORT -> "0"
            DECIMAL -> "BigDecimal.ZERO"
            BOOLEAN, DATE, TIME, DATE_TIME, OTHER -> "null"
            BINARY -> "byteArrayOf()"
            SET -> "[]"
            JSON -> "{}"
            else -> "null"
        }
    }

    companion object {
        fun <T : Any> of(type: KClass<T>): DbType {
            return of(type.java);
        }

        @JvmStatic
        fun <T> of(type: Class<T>): DbType {

            if (type.isEnum) {
                return ENUM;
            }

            DbType.values()
                .firstOrNull { it.javaType == type || (it.javaRefType != null && it.javaRefType == type) }
                .apply {
                    if (this != null) {
                        return this;
                    }
                }

            if (type == Character::class.java || type == java.lang.Character::class.java) {
                return STRING
            }

            if (type == DATE::class.java) {
                return DATE_TIME
            }

            if (Map::class.java.isAssignableFrom(type)) {
                return JSON
            }
            if (type.IsCollectionType) {
                return JSON
            }

            return OTHER
        }
    }
}