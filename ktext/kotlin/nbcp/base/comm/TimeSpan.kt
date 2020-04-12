package nbcp.comm

import nbcp.comm.AsInt
import java.time.Duration
import java.time.LocalDateTime


data class TimeSpan(val totalMilliseconds: Long = 0) {
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

    /**
     * 获取毫秒部分
     */
    var milliseconds: Int = 0
        get
        private set


    val totalHours: Int
        get() = (totalMilliseconds / 3600000).AsInt()

    val totalMinutes: Int
        get() = (totalMilliseconds / 60000).AsInt()

    val totalSeconds: Long
        get() = totalMilliseconds / 1000

    init {
        if (totalMilliseconds > 0) {
            var left = totalMilliseconds;
            this.milliseconds = (left % 1000).AsInt()
            left = left / 1000;

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
        if (milliseconds > 0) {
            ret.add("${milliseconds}毫秒")
        }
        return ret.joinToString("");
    }
}


fun Duration.toSummary(): String {
    return TimeSpan(this.toMillis()).toString();
}

/**
 * 重载运算符， 两个时间相减： time1 - time2
 */
operator fun LocalDateTime.minus(beforeTime: LocalDateTime): TimeSpan {
    return TimeSpan(Duration.between(beforeTime, this).toMillis())
}
