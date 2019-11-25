package nbcp.base

import nbcp.base.extend.AsInt
import java.time.Duration
import java.time.LocalDateTime


data class TimeSpan(val totalSeconds: Long = 0) {
    var days: Int = 0
        get
        private set

    var hours: Int = 0
        get
        private set

    var minutes: Int = 0
        get
        private set

    var seconds: Int = 0
        get
        private set

    val totalMinutes: Int
        get() = (totalSeconds / 60).AsInt()

    val totalHours: Int
        get() = (totalSeconds / 3600).AsInt()

    init {
        if (totalSeconds > 0) {
            var left = totalSeconds;

            this.seconds = (left % 60).AsInt();
            left = left / 60;

            this.minutes = (left % 60).AsInt()
            left = left / 60;

            this.hours = (left % 24).AsInt()
            left = left / 24;

            this.days = left.AsInt()
        }
    }

    override fun toString(): String {
        var ret = mutableListOf<String>();
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
}


fun Duration.toSummary(): String {
    return TimeSpan(this.seconds).toString();
}


operator fun LocalDateTime.minus(beforeTime: LocalDateTime): TimeSpan {
    return TimeSpan(Duration.between(beforeTime, this).seconds)
}
