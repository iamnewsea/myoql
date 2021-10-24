package nbcp.base

import nbcp.TestBase
import nbcp.comm.GroupLog
import nbcp.comm.*
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.boot.logging.LogLevel
import java.time.Duration
import java.time.LocalDateTime

@GroupLog("main")
class TestDuration : TestBase() {
    companion object{
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }
    @Test
    fun Test_Duration() {
        var d = Duration.parse("PT20M");
        println(d.seconds)
        usingScope(LogLevelScope.info) {
            logger.info(d.seconds.toString())
        }

        var info = LogLevel.INFO;

        println(info.name)
    }

    @Test
    fun Test_Duration2() {
        var d = LocalDateTime.now().minus("2020-9-14 12:00:00".AsLocalDateTime()!!);
        println(d.seconds)
    }
    @Test
    fun Test_LocalTime() {
        println(LocalDateTime.now().AsLocalTime())
    }
    @Test
    fun b() {
        val j = JsonMap();
        for (i in 1..48) {
            j.put("k" + i, "value" + i)
        }
        println(j.get("k32"))
    }
}