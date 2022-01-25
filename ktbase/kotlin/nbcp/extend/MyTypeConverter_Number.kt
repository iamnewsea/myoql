@file:JvmName("MyHelper")
@file:JvmMultifileClass

package nbcp.comm

import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*


/**
 * 转为Int。
 * Char转Int，按字符串转。
 * Boolean，tru -> 1 , false->0
 * 字符串，先转 toBigDecimal 再转 Int
 */
@JvmOverloads
fun Any?.AsInt(defaultValue: Int = 0): Int {
    if (this == null) return defaultValue;
    var ret = defaultValue;


    try {
        if (this is Int) {
            if (this == 0) return defaultValue;
            return this;
        } else if (this is Number) {
            var d = this.toInt()
            if (d == 0) return defaultValue;
            return d
        } else if (this is Boolean) {
            if (this == true) return 1;
            return 0;
        } else {
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
                if (strValue.isEmpty()) return defaultValue


                if (strValue.length > 2 && strValue[0] == '0' && strValue[1].uppercaseChar() == 'X') {
                    ret = strValue.substring(2).toInt(16);
                } else {
                    ret = strValue.toBigDecimal().toInt()
                }

                return ret;
            }
        }

        if (ret == 0) return defaultValue;
        return ret;
    } catch (e: java.lang.Exception) {
        return defaultValue;
    }
}

@JvmOverloads
fun Any?.AsLong(defaultValue: Long = 0L): Long {
    if (this == null) return defaultValue;

    try {
        var ret = defaultValue;
        if (this is Long) {
            if (this == 0L) return defaultValue;
            return this;
        } else if (this is Number) {
            var l = this.toLong();
            if (l == 0L) return defaultValue;
            return l;
        } else {
            var value = this;

            if (value is CharSequence) {
                value = value.toString()
            }
            if (value is String) {
                var strValue = value.trim()
                if (strValue.isEmpty()) return defaultValue


                if (strValue.length > 2 && strValue[0] == '0' && strValue[1].uppercaseChar() == 'X') {
                    ret = strValue.substring(2).toLong(16)
                } else {
                    ret = strValue.toLongOrNull() ?: defaultValue
                }
                return ret;
            }
        }

        if (ret == 0L) return defaultValue;
        return ret;
    } catch (e: Exception) {
        return defaultValue;
    }
}

@JvmOverloads
fun Any?.AsDouble(defaultValue: Double = 0.0): Double {
    if (this == null) return defaultValue;
    var ret = defaultValue;

    try {
        if (this is Double) {
            if (this == 0.0) return defaultValue;
            return this;
        } else if (this is Number) {
            var d = this.toDouble();
            if (d == 0.0) return defaultValue;
            return d;
        } else {
            var value = this;

            if (value is CharSequence) {
                value = value.toString()
            }
            if (value is String) {
                var strValue = value.trim();
                if (strValue.isEmpty()) return defaultValue

                ret = strValue.toDoubleOrNull() ?: defaultValue
                return ret;
            }
        }

        if (ret == 0.0) return defaultValue;
        return ret
    } catch (e: Exception) {
        return defaultValue;
    }
}

@JvmOverloads
fun Any?.AsFloat(defaultValue: Float = 0F): Float {
    if (this == null) return defaultValue;
    var ret = defaultValue;

    try {
        if (this is Float) {
            if (this == 0F) return defaultValue;
            return this;
        } else if (this is Number) {
            var f = this.toFloat();
            if (f == 0F) return defaultValue;
            return f;
        } else {
            var value = this;
            if (value is CharSequence) {
                value = value.toString()
            }

            if (value is String) {
                var strValue = value.trim();
                if (strValue.isEmpty()) return defaultValue
                ret = strValue.toFloatOrNull() ?: defaultValue
                return ret;
            }
        }

        if (ret == 0F) return defaultValue;
        return ret;
    } catch (e: Exception) {
        return defaultValue;
    }
}


fun Any?.AsBigDecimal(): BigDecimal? {
    if (this == null) return null;
    if (this is BigDecimal) return this;

    if (this is Number) {
        return this.toDouble().toBigDecimal()
    }
    if (this is String) {
        return this.toBigDecimal()
    }

    throw java.lang.RuntimeException("不识别的数据类型:${this::class.java}")
}


/**
 * 为代码生成器而扩展
 */
val Number?.HasValue: Boolean
    @JvmName("hasValue")
    get() {
        return this != null && this.toDouble() != 0.0
    }