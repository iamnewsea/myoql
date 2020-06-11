@file:JvmName("MyHelper")
@file:JvmMultifileClass

package nbcp.comm

import java.text.DecimalFormat
import java.time.*
import java.util.*


fun LocalDate.format(pattern: String): String {
    return this.format(java.time.format.DateTimeFormatter.ofPattern(pattern));
}

fun LocalDateTime.format(pattern: String): String {
    return this.format(java.time.format.DateTimeFormatter.ofPattern(pattern));
}
fun LocalTime.format(pattern: String): String {
    return this.format(java.time.format.DateTimeFormatter.ofPattern(pattern));
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

infix fun LocalDate.min(other: LocalDate): LocalDate {
    if (this <= other) return this;
    return other;
}

infix fun LocalDate.max(other: LocalDate): LocalDate {
    if (this <= other) return other;
    return this;
}


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


