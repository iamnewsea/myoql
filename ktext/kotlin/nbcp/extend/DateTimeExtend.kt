@file:JvmName("MyHelper")
@file:JvmMultifileClass

package nbcp.comm

import nbcp.utils.MyUtil
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.time.*
import java.util.*

@JvmOverloads
fun LocalDate.Format(pattern: String = ""): String {
    if (this == LocalDate.MIN) return "";
    return this.format(java.time.format.DateTimeFormatter.ofPattern(pattern.AsString("yyyy-MM-dd")));
}


@JvmOverloads
fun LocalDateTime.Format(pattern: String = ""): String {
    if (this == LocalDateTime.MIN) return "";
    if (this.hour == 0 && this.minute == 0 && this.second == 0) {
        return this.format(java.time.format.DateTimeFormatter.ofPattern(pattern.AsString("yyyy-MM-dd")));
    }
    var zoneSecond = 0L;
    if (pattern.contains("Z")) {
        zoneSecond = ZoneId.systemDefault().rules.getOffset(Instant.EPOCH).totalSeconds.AsLong()
    }
    return this.minusSeconds(zoneSecond)
        .format(java.time.format.DateTimeFormatter.ofPattern(pattern.AsString("yyyy-MM-dd HH:mm:ss")));
}

/**
 * @param pattern: 最全格式 HH:mm:ss.SSS
 */
@JvmOverloads
fun LocalTime.Format(pattern: String = ""): String {
    if (this == LocalTime.MIN) {
        return "";
    }
    return this.format(java.time.format.DateTimeFormatter.ofPattern(pattern.AsString("HH:mm:ss")));
}


fun java.util.Date.Format(pattern: String): String {
    if (this.time == 0L) {
        return "";
    }

    var time = this;
    if (time.time % (MyUtil.OneDaySeconds * 1000) == 0L) {
        return SimpleDateFormat(pattern.AsString("yyyy-MM-dd")).format(time);
    }

    var zoneSecond = 0L;
    if (pattern.contains("Z")) {
        zoneSecond = ZoneId.systemDefault().rules.getOffset(Instant.EPOCH).totalSeconds.AsLong()
        time = Date(time.time - zoneSecond)
    }
    return SimpleDateFormat(pattern.AsString("yyyy-MM-dd HH:mm:ss")).format(time)
}

fun LocalDate.atEndOfDay(): LocalDateTime {
    return this.atTime(23, 59, 59, 999_999_999);
}

fun LocalDate.isBefore(value: LocalDateTime): Boolean {
    return this.atEndOfDay().isBefore(value);
}

fun LocalDateTime.plusSeconds(value: Int): LocalDateTime {
    return this.plusSeconds(value.toLong());
}

infix fun LocalDate.getMin(other: LocalDate): LocalDate {
    if (this <= other) return this;
    return other;
}

infix fun LocalDate.getMax(other: LocalDate): LocalDate {
    if (this <= other) return other;
    return this;
}

@JvmOverloads
fun LocalDateTime.AsDate(defaultValue: Date = Date(0)): Date {
    if (this.year < 0) return defaultValue
    return Date.from(this.atZone(ZoneId.systemDefault()).toInstant());
}

/**
 * 时间转为毫秒数
 */
fun LocalDateTime.ToLong(): Long {
    return this.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
}


/**
 * 返回描述信息
 */
fun Duration.toSummary(): String {
    var ret = mutableListOf<String>();
    var seconds = this.seconds
    var days = seconds / 86400;
    seconds = seconds % 86400;
    var hours = seconds / 3600;
    seconds = seconds % 3600;
    var minutes = seconds / 60;
    seconds = seconds % 60;

    if (days > 0) {
        ret.add("${days}天")
    }

    if (hours > 0) {
        ret.add("${hours}小时")
    }
    if (minutes > 0) {
        ret.add("${minutes}分钟")
    }
    if (seconds > 0) {
        ret.add("${seconds}秒")
    }
    return ret.joinToString("");
}

/**
 * 重载运算符， 两个时间相减： time1 - time2
 */
operator fun LocalDateTime.minus(beforeTime: LocalDateTime): Duration {
    return Duration.between(beforeTime, this)
}

