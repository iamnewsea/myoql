package nbcp.extend

import nbcp.TestBase
import nbcp.comm.*
import nbcp.utils.CookieData
import org.junit.jupiter.api.Test
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import nbcp.scope.*
import nbcp.utils.MyUtil
import java.time.Duration
import java.time.format.DateTimeFormatter

class TestTime : TestBase() {
    @Test
    fun TestSummary() {
        var t = Duration.ofNanos(
            MyUtil.getRandomWithMaxValue(999999).AsLong() * MyUtil.getRandomWithMaxValue(999999).AsLong()
        )
        println(t.toSummary())
    }
}