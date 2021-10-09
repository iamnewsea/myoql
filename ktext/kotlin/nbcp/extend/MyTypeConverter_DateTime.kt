@file:JvmName("MyHelper")
@file:JvmMultifileClass

package nbcp.comm

import org.slf4j.LoggerFactory
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*

private val logger = LoggerFactory.getLogger("ktext.MyObject.DateTimeConverter")

fun Any?.AsLocalDate(): LocalDate? {
    return this.AsLocalDateTime()?.toLocalDate();
}


fun Any?.AsLocalTime(): LocalTime? {
    if (this == null) {
        return null;
    }

    if (this is LocalTime) {
        return this
    }

    if (this is LocalDate) {
        return LocalTime.MIN;
    }

    if (this is LocalDateTime) {
        return this.toLocalTime();
    }

    var strValue: String;

    if (this is String) {
        strValue = this
    } else if (this is CharSequence) {
        strValue = this.toString();
    } else if (this is Calendar) {
        return LocalTime.of(this.get(Calendar.HOUR_OF_DAY), this.get(Calendar.MINUTE), this.get(Calendar.SECOND), this.get(Calendar.MILLISECOND))
    } else if (this is Date) {
        //当地时间。
        var calendar = Calendar.getInstance();
        calendar.time = this;
        return LocalTime.of(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND), calendar.get(Calendar.MILLISECOND))
//        return LocalTime.of(this.hours, this.minutes, this.seconds, (this.time % 1000).AsInt() * 1000000)
    } else {
        throw RuntimeException("非法的类型转换,试图从 ${this::class.java}类型 到 LocalTime类型")
    }

    if (strValue.length < 5 && !strValue.any { it == ':' }) {
        return null;
    }

    return strValue.ConvertToLocalTime()
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
    } else {
        throw RuntimeException("非法的类型转换,试图从 ${this::class.java}类型 到 LocalDateTime类型")
    }

    strValue = strValue.trim();
    if (strValue.length < 8) {
        return null;
    }


    try {
        return strValue.ConvertToLocalDateTime();
    } catch (e: Exception) {
        logger.error(e.message, e);
        return null
    }
}

val LocalTime.totalSeconds: Int
    get() {
        return this.hour * 3600 + this.minute * 60 + this.second
    }

/**
 * 字符串转为 LocalDateTime
 */
@JvmOverloads
fun String.ConvertToLocalDateTime(dateTimeFormatter: DateTimeFormatter? = null): LocalDateTime? {
    //关于 160空格：https://blog.csdn.net/lewky_liu/article/details/79353151
    //本函数不做处理。
    var strValue = this.trim();

    if (dateTimeFormatter != null) {
        return LocalDateTime.parse(strValue, dateTimeFormatter)
    }

    // Fri, 04 Dec 2020 03:11:42 GMT
    // DateTimeFormatter.RFC_1123_DATE_TIME
    if (this.endsWith("GMT") && this.contains(",")) {
        return LocalDateTime.parse(this, DateTimeFormatter.RFC_1123_DATE_TIME);
    }

    var withZ = strValue.endsWith('Z')

    if (withZ) {
        strValue = strValue.Slice(0, -1);
    }

    //分成两部分。 找冒号前面找字母或空格 ,T, 'T'
    var timeSignIndex = strValue.indexOf(':');
    if (timeSignIndex < 0) {
        return strValue.ConvertToLocalDate()?.atStartOfDay()
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

    var timezoneOffset = 0
    var timezoneValueMatch = """([+|-])(\d\d:\d\d)$""".toRegex().find(timePartString)
    if (timezoneValueMatch != null && timezoneValueMatch.groupValues.size > 2) {
        timezoneOffset = (timezoneValueMatch.groupValues[2] + ":00").AsLocalTime()?.totalSeconds ?: 0

        if (timezoneValueMatch.groupValues[1] == "-") {
            timezoneOffset = 0 - timezoneOffset
        }

        timePartString = timePartString.substring(0, timezoneValueMatch.range.start)


        zoneSecond = ZoneId.systemDefault().rules.getOffset(Instant.EPOCH).totalSeconds - timezoneOffset
    }

    return datePartString.ConvertToLocalDate()?.atTime(timePartString.ConvertToLocalTime())?.plusSeconds(zoneSecond)
}


/**
 * 转换为 LocalDate,自动识别以下格式
yyyyMMdd
yyyy-MM-dd
yyyy/MM/dd
yyyy_MM_dd
yyyy.MM.dd
 */
@JvmOverloads
fun String.ConvertToLocalDate(dateFormatter: DateTimeFormatter? = null): LocalDate? {
    var strValue = this.trim();

    if (dateFormatter != null) {
        return LocalDate.parse(strValue, dateFormatter);
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
@JvmOverloads
fun String.ConvertToLocalTime(timeFormatter: DateTimeFormatter? = null): LocalTime {
    var timeString = this.trim();

    if (timeFormatter != null) {
        return LocalTime.parse(timeString, timeFormatter);
    }
    timeString = timeString.split(' ').last();
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
