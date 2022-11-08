package nbcp.base.extend

import nbcp.base.TestBase
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import nbcp.base.utils.MyUtil
import java.time.Duration

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