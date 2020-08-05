@file:JvmName("MyHelper")
@file:JvmMultifileClass

package nbcp.comm

import java.math.BigDecimal
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
        if (clazz is List<*>) {
            (this as Array<*>).toMutableList();
        }
        if (clazz is Set<*>) {
            (this as Array<*>).toMutableSet();
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
 * 转为Int。
 * Char转Int，按字符串转。
 * Boolean，tru -> 1 , false->0
 * 字符串，先转 toBigDecimal 再转 Int
 */
fun Any?.AsInt(defaultValue: Int = 0): Int {
    if (this == null) return defaultValue;
    var ret = defaultValue;


    try {
        if (this is Int) ret = this;
        else if (this is Number) ret = this.toInt();
        else if (this is Boolean) {
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


                if (strValue.length > 2 && strValue[0] == '0' && strValue[1].toUpperCase() == 'X') {
                    ret = strValue.substring(2).toInt(16);
                } else {
                    ret = strValue.toBigDecimal().toInt()
                }
            }
        }

        if (ret != 0) {
            return ret;
        }
        return defaultValue
    } catch (e: java.lang.Exception) {
        return defaultValue;
    }
}

/**
 * @param format 时间格式化字符串，数字的话也可以传 0.0,#.#
 */
fun Any?.AsString(defaultValue: String = "", format: String = ""): String {
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
            return this.format(java.time.format.DateTimeFormatter.ofPattern(format.AsString("yyyy-MM-dd")));
        }
        return this.format(java.time.format.DateTimeFormatter.ofPattern(format.AsString("yyyy-MM-dd HH:mm:ss")));
    } else if (this is LocalDate) {
        if (this == LocalDate.MIN) {
            return "";
        }
        return this.format(java.time.format.DateTimeFormatter.ofPattern(format.AsString("yyyy-MM-dd")));
    } else if (this is LocalTime) {
        if (this == LocalTime.MIN) {
            return "";
        }
        return this.format(java.time.format.DateTimeFormatter.ofPattern(format.AsString("HH:mm:ss")));
    } else if (this is Date) {
        if (this.time == 0L) {
            return "";
        }
        return SimpleDateFormat(format.AsString("yyyy-MM-dd HH:mm:ss")).format(this);
    } else if (this is Number) {
        //TODO  格式化 0.0 , #.#

    }

    var ret = this.toString()
    if (ret.isNullOrEmpty()) return defaultValue
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

fun Any?.AsLong(defaultValue: Long = 0): Long {
    if (this == null) return defaultValue;

    try {
        var ret = defaultValue;
        if (this is Long) ret = this;
        else if (this is Number) ret = this.toLong()
        else {
            var value = this;

            if (value is CharSequence) {
                value = value.toString()
            }
            if (value is String) {
                var strValue = value.trim()
                if (strValue.isEmpty()) return defaultValue


                if (strValue.length > 2 && strValue[0] == '0' && strValue[1].toUpperCase() == 'X') {
                    ret = strValue.substring(2).toLong(16)
                } else {
                    ret = strValue.toLongOrNull() ?: defaultValue
                }
            }
        }

        if (ret != 0L) {
            return ret;
        }
        return defaultValue;
    } catch (e: Exception) {
        return defaultValue;
    }

}

fun Any?.AsDouble(defaultValue: Double = 0.0): Double {
    if (this == null) return defaultValue;
    var ret = defaultValue;

    try {
        if (this is Double) ret = this;
        else if (this is Number) ret = this.toDouble()
        else {
            var value = this;

            if (value is CharSequence) {
                value = value.toString()
            }
            if (value is String) {
                var strValue = value.trim();
                if (strValue.isEmpty()) return defaultValue

                ret = strValue.toDoubleOrNull() ?: defaultValue
            }
        }

        if (ret != 0.0) {
            return ret;
        }
        return defaultValue;
    } catch (e: Exception) {
        return defaultValue;
    }
}

fun Any?.AsFloat(defaultValue: Float = 0F): Float {
    if (this == null) return defaultValue;
    var ret = defaultValue;

    try {
        if (this is Float) ret = this;
        else if (this is Number) ret = this.toFloat()
        else {
            var value = this;
            if (value is CharSequence) {
                value = value.toString()
            }

            if (value is String) {
                var strValue = value.trim();
                if (strValue.isEmpty()) return defaultValue
                ret = strValue.toFloatOrNull() ?: defaultValue
            }
        }

        if (ret != 0F) {
            return ret;
        }
        return defaultValue;
    } catch (e: Exception) {
        return defaultValue;
    }
}


fun Any?.AsLocalDate(): LocalDate? {
    return this.AsLocalDateTime()?.toLocalDate();
}


fun Any?.AsLocalTime(defaultVale: LocalTime = LocalTime.MIN): LocalTime {
    if (this == null) {
        return defaultVale;
    }

    if (this is LocalTime) {
        return this
    }

    if (this is LocalDate) {
        return defaultVale;
    }

    if (this is LocalDateTime) {
        return this.toLocalTime();
    }

    var strValue = "";

    if (this is String) {
        strValue = this
    } else if (this is CharSequence) {
        strValue = this.toString();
    } else if (this is Date) {
        //当地时间。
        return LocalTime.of(this.hours, this.minutes, this.seconds, (this.time % 1000).AsInt() * 1000000)
    } else {
        throw RuntimeException("非法的类型转换,试图从 ${this::class.java}类型 到 LocalTime类型")
    }

    if (strValue.length < 5 && !strValue.any { it == ':' }) {
        return defaultVale;
    }


    return ConvertAsLocalTime(strValue)
}

fun Any?.AsLocalDateTime(): LocalDateTime? {
    if (this == null) {
        return null;
    }

    if (this is LocalDate) {
        return this.atStartOfDay();
    }

    if (this is LocalDateTime) {
        return this;
    }

    var strValue = "";

    if (this is String) {
        strValue = this
    } else if (this is CharSequence) {
        strValue = this.toString();
    } else if (this is java.sql.Date) {
        return this.toLocalDate().atStartOfDay();
    } else if (this is java.sql.Time) {
        return this.toLocalTime().atDate(LocalDate.of(0, 1, 1))
    } else if (this is java.sql.Timestamp) {
        return this.toLocalDateTime()
    } else if (this is Date) {
        return LocalDateTime.ofInstant(this.toInstant(), ZoneId.systemDefault());
        //return LocalDateTime.of(this.year + 1900, this.month + 1, this.date, this.hours, this.minutes, this.seconds, (this.time % 1000).AsInt() * 1000000)
//        var value = this.time;
//
//        var ret = LocalDate.of(1970, 1, 1).atStartOfDay();
//
////        //按北京时间处理。
////        ret = ret.plusHours(8);
//        ret = ret.plusSeconds(value / 1000)
//        ret = ret.plusNanos(value % 1000 * 1000000)
//
//        //当地时间。
//        return ret;
//        return LocalDateTime.ofInstant(this.toInstant(), ZoneId.systemDefault());
    } else {
        throw RuntimeException("非法的类型转换,试图从 ${this::class.java}类型 到 LocalDateTime类型")
    }

    strValue = strValue.trim();
    if (strValue.length < 8) {
        return null;
    }


    try {
        //正则判断日期格式.
        /**
        YYYYMMDD
        YYYY-MM-DD
        YYYY/MM/DD
        YYYY_MM_DD
        YYYY.MM.DD
         */

        var withZ = strValue.endsWith('Z')

        if (withZ) {
            strValue = strValue.Slice(0, -1);
        }

        //分成两部分。 找冒号前面找字母或空格 ,T, 'T'
        var timeSignIndex = strValue.indexOf(':');
        if (timeSignIndex < 0) {
            return ConvertAsLocalDate(strValue)?.atStartOfDay()
        }

        var fenIndex = strValue.substring(0, timeSignIndex).indexOfFirst { it == ' ' || it.isLetter() }
        if (fenIndex < 0) {
            throw RuntimeException("不正确的时间格式:${strValue}")
        }

        var wrappeT = false;
        if (fenIndex > 1 && fenIndex < strValue.length - 1) {
            if (strValue[1].isDigit() == false && strValue[fenIndex - 1] == strValue[fenIndex + 1]) {
                wrappeT = true;
            }
        }

        var datePartString = "";
        var timePartString = "";
        if (wrappeT) {
            datePartString = strValue.substring(0, fenIndex - 1);
            strValue = strValue.substring(fenIndex + 2);
        } else {
            datePartString = strValue.substring(0, fenIndex);
            strValue = strValue.substring(fenIndex + 1);
        }

        if (withZ) {
            timePartString = strValue.Slice(0, -1)
        } else {
            timePartString = strValue
        }

        var zoneSecond = 0;
        if (withZ) {
            zoneSecond = ZoneId.systemDefault().rules.getOffset(Instant.EPOCH).totalSeconds
        }

        return ConvertAsLocalDate(datePartString)?.atTime(ConvertAsLocalTime(timePartString))?.plusSeconds(zoneSecond)

    } catch (e: Exception) {
        logger.error(e.message, e);
        return null
    }
}

/**
 * 转换为 LocalDate
 */
fun ConvertAsLocalDate(dateString: String, dateFormat: String = ""): LocalDate? {
    var strValue = dateString.trim();

    if (dateFormat.HasValue) {
        return LocalDate.parse(strValue, DateTimeFormatter.ofPattern(dateFormat));
    }

    if (strValue.length == 8 && !strValue.any { it.isDigit() == false }) {
        var ret = LocalDate.parse(strValue, DateTimeFormatter.ofPattern("yyyyMMdd"))
        return ret
    }

    var fen = strValue[4];

    if (fen != '-' && fen != '/' && fen != '_' && fen != '.') {
        return null;
    }

    var sects = strValue.split(fen);
    if (sects.size != 3) {
        throw java.lang.RuntimeException("不识别的日期格式: ${strValue}")
    }

    var year = sects[0].AsInt();
    var month = sects[1].AsInt();
    var day = sects[2].AsInt();

    return LocalDate.of(year, month, day);
}


/**
 * 转换为 LocalTime
 */
fun ConvertAsLocalTime(timeString: String, timeFormat: String = ""): LocalTime {
    var timeString = timeString.trim();

    if (timeFormat.HasValue) {
        return LocalTime.parse(timeString, DateTimeFormatter.ofPattern(timeFormat));
    }

    var nanos = 0L;
    var dotIndex = timeString.indexOf('.');
    if (dotIndex >= 0) {
        nanos = timeString.substring(dotIndex + 1).AsLong() * 1000000;
        timeString = timeString.substring(0, dotIndex);
    }

    var sects = timeString.split(':');
    var hour = sects[0].AsInt();
    var minute = sects[1].AsInt();
    var second = 0;
    if (sects.size > 2) {
        second = sects[2].AsInt();
    }

    return LocalTime.of(hour, minute, second).plusNanos(nanos)
}

fun Any?.AsDate(): Date? {
    if (this == null) return null;

    if (this is Date) {
        return this
    } else if (this is LocalDate) {
        if (this.year < 0) return null
        var c = Calendar.getInstance(TimeZone.getTimeZone("GMT+:08:00"))
        c.set(this.year, this.monthValue - 1, this.dayOfMonth, 0, 0, 0)
        c.set(Calendar.MILLISECOND, 0)
        return c.time

    } else if (this is LocalDateTime) {
        if (this.year < 0) return null

        var c = Calendar.getInstance(TimeZone.getTimeZone("GMT+:08:00"))
        c.set(this.year, this.monthValue - 1, this.dayOfMonth, this.hour, this.minute, this.second)
        c.set(Calendar.MILLISECOND, this.nano * 1000000)
        return c.time
    } else {
        var value = this;

        if (value is CharSequence) {
            value = value.toString()
        }

        if (value is String) {
            return value.AsLocalDateTime().AsDate()
        }
    }

    return null
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


