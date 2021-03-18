package nbcp.comm

import nbcp.TestBase
import nbcp.comm.*
import nbcp.utils.CookieData
import org.junit.Test
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class TypeTest : TestBase() {
    @Test
    fun test_type2_convert() {
        println(TypeTest::class.isFun)
    }

    @Test
    fun test_time() {
        // seconds == totalSeconds
        println(Duration.ofDays(3).seconds)
    }
}