@file:JvmName("MyHelper")
@file:JvmMultifileClass

package nbcp.comm

import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*

private val logger = LoggerFactory.getLogger("nbcp.comm.MyHelper")

/**
 * 转为Int。
 * Char转Int，按字符串转。
 * Boolean，tru -> 1 , false->0
 * 字符串，先转 toBigDecimal 再转 Int
 */
@JvmOverloads
fun Any?.AsInt(defaultValue: Int = 0): Int {
    this.AsIntWithNull()
        .apply {
            if (this == null) return defaultValue
            if (this == 0) return 0
            return this;
        }
}


fun Any?.AsIntWithNull(): Int? {
    if (this == null) return null;

    if (this is Int) {
        return this;
    } else if (this is Number) {
        return this.toInt()
    } else if (this is Boolean) {
        if (this == true) return 1;
        return 0;
    }

    try {
        var value = this;

        if (value is CharSequence) {
            value = value.toString();
        }
        //特殊处理一下。
        //Char的AsInt
        else if (value is Char) {
            value = value.toString()
        }

        if (value is String) {
            var strValue = value.trim()
            if (strValue.isEmpty()) return null

            if (strValue.length > 2 && strValue[0] == '0' && strValue[1].uppercaseChar() == 'X') {
                return strValue.substring(2).toInt(16);
            }
            return strValue.toBigDecimal().toInt()
        }

        logger.Important("AsIntWithNull 不识别的数据类型${this::class.java.name}")
    } catch (e: java.lang.Exception) {
        logger.error(e.message, e);
    }
    return null;
}


@JvmOverloads
fun Any?.AsLong(defaultValue: Long = 0L): Long {
    this.AsLongWithNull()
        .apply {
            if (this == null) return defaultValue
            if (this == 0L) return 0L
            return this;
        }

}


fun Any?.AsLongWithNull(): Long? {
    if (this == null) return null;

    if (this is Long) {
        return this;
    } else if (this is Number) {
        return this.toLong();
    }
    try {

        var value = this;

        if (value is CharSequence) {
            value = value.toString()
        }

        if (value is String) {
            var strValue = value.trim()
            if (strValue.isEmpty()) return null


            if (strValue.length > 2 && strValue[0] == '0' && strValue[1].uppercaseChar() == 'X') {
                return strValue.substring(2).toLong(16)
            }
            return strValue.toLongOrNull()
        }

        logger.Important("AsLongWithNull 不识别的数据类型${this::class.java.name}")
    } catch (e: Exception) {
        logger.error(e.message, e);
    }

    return null;
}


@JvmOverloads
fun Any?.AsDouble(defaultValue: Double = 0.0): Double {
    this.AsDoubleWithNull()
        .apply {
            if (this == null) return defaultValue
            if (this == 0.0) return 0.0
            return this;
        }
}


fun Any?.AsDoubleWithNull(): Double? {
    if (this == null) return null;
    if (this is Double) {
        return this;
    } else if (this is Number) {
        return this.toDouble();
    }

    try {

        var value = this;

        if (value is CharSequence) {
            value = value.toString()
        }

        if (value is String) {
            var strValue = value.trim();
            if (strValue.isEmpty()) return null

            return strValue.toDoubleOrNull()
        }
        logger.Important("AsDoubleWithNull 不识别的数据类型${this::class.java.name}")
    } catch (e: Exception) {
        logger.error(e.message, e);
    }
    return null;
}

@JvmOverloads
fun Any?.AsFloat(defaultValue: Float = 0F): Float {
    this.AsFloatWithNull()
        .apply {
            if (this == null) return defaultValue
            if (this == 0F) return 0F
            return this;
        }
}


fun Any?.AsFloatWithNull(): Float? {
    if (this == null) return null;

    if (this is Float) {
        return this;
    } else if (this is Number) {
        return this.toFloat();
    }

    try {

        var value = this;
        if (value is CharSequence) {
            value = value.toString()
        }

        if (value is String) {
            var strValue = value.trim();
            if (strValue.isEmpty()) return null
            return strValue.toFloatOrNull()

        }

        logger.Important("AsFloatWithNull 不识别的数据类型${this::class.java.name}")
    } catch (e: Exception) {
        logger.error(e.message, e)
    }

    return null;
}

fun Any?.AsBigDecimal(defaultValue: BigDecimal = BigDecimal.ZERO): BigDecimal {
    this.AsBigDecimalWithNull()
        .apply {
            if (this == null) return defaultValue
            if (this == BigDecimal.ZERO) return BigDecimal.ZERO
            return this;
        }
}

fun Any?.AsBigDecimalWithNull(): BigDecimal? {
    if (this == null) return null;
    if (this is BigDecimal) return this;

    if (this is Number) {
        return this.toDouble().toBigDecimal()
    }
    if (this is String) {
        return this.toBigDecimal()
    }

    logger.Important("AsBigDecimalWithNull 不识别的数据类型:${this::class.java}")
    return null;
}


/**
 * 为代码生成器而扩展
 */
val Number?.HasValue: Boolean
    @JvmName("hasValue")
    get() {
        return this != null && this.toDouble() != 0.0
    }