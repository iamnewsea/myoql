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
            MyUtil.getRandomNumber(0,999999).AsLong() * MyUtil.getRandomNumber(0,999999).AsLong()
        )
        println(t.toSummary())
    }

    @Test
    fun TestNumberString() {
        println(LocalDateTime.now().toNumberString())

        println("20220124151902926".ConvertToLocalDateTime())
    }
}