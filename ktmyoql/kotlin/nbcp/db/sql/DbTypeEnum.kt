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
    String(kotlin.String::class.java, Types.VARCHAR),
    Enum(kotlin.String::class.java, Types.VARCHAR),
    Int(kotlin.Int::class.java, Types.INTEGER, java.lang.Integer::class.java),
    Float(kotlin.Float::class.java, Types.FLOAT, java.lang.Float::class.java),
    Long(kotlin.Long::class.java, Types.BIGINT, java.lang.Long::class.java),
    Double(kotlin.Double::class.java, Types.DOUBLE, java.lang.Double::class.java),
    Byte(kotlin.Byte::class.java, Types.TINYINT, java.lang.Byte::class.java),
    Short(kotlin.Short::class.java, Types.SMALLINT, java.lang.Short::class.java),
    Boolean(kotlin.Boolean::class.java, Types.BIT, java.lang.Boolean::class.java),
    Decimal(BigDecimal::class.java,Types.DECIMAL),
    Date(LocalDate::class.java, Types.DATE),
    Time(LocalTime::class.java, Types.TIME),
    DateTime(LocalDateTime::class.java, Types.TIMESTAMP),
    Binary(ByteArray::class.java, Types.VARBINARY),

    //比如sql表达式.
    Other(Any::class.java, Types.OTHER);


    var javaType: Class<*>;
    var javaRefType: Class<*>?;
    var sqlType:kotlin.Int

    constructor(javaType: Class<*>, sqlType: kotlin.Int, javaRefType: Class<*>? = null) {
        this.javaType = javaType;
        this.sqlType = sqlType;
        this.javaRefType = javaRefType;
    }

    //是否是Text类型. 需要 单引号包裹
    //Other 是复合列。
    fun needTextWrap(): kotlin.Boolean {
        return this == DbType.String || this == DbType.Enum || this.isDateOrTime();//|| this == DbType.Other;
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