package nbcp.base.utils

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

object DateTimeUtil {

    fun fromTotalSeconds(totalSeconds: Long): LocalDateTime {
        return LocalDateTime.ofEpochSecond(totalSeconds, 0, ZoneId.systemDefault().rules.getOffset(Instant.EPOCH))
    }
}