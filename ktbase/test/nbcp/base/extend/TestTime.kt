package nbcp.base.extend

import nbcp.base.TestBase
import nbcp.base.utils.MyUtil
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.LocalDateTime

class TestTime : TestBase() {
    @Test
    fun TestSummary() {
        var t = Duration.ofNanos(
            MyUtil.getRandomNumber(0, 999999).AsLong() * MyUtil.getRandomNumber(0, 999999).AsLong()
        )
        println(t.toSummary())
    }

    @Test
    fun TestNumberString() {
        println(LocalDateTime.now().toNumberString())

        println("20220124151902926".ConvertToLocalDateTime())
    }
}