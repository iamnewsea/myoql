package nbcp.base

import nbcp.base.annotation.*
import nbcp.base.comm.JsonMap
import nbcp.base.enums.LogLevelScopeEnum
import nbcp.base.extend.AsLocalDateTime
import nbcp.base.extend.AsString
import nbcp.base.extend.minus
import nbcp.base.extend.usingScope
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.boot.logging.LogLevel
import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.ChronoField


@GroupLog("main")
class TestDuration : TestBase() {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    @Test
    fun Test_Duration() {
        var d = Duration.parse("PT20M");
        println(d.seconds)
        usingScope(LogLevelScopeEnum.info) {
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
        var now = LocalDateTime.now()
        println(now.AsString())

        now.get(ChronoField.YEAR).apply { println(this) }
        now.get(ChronoField.MONTH_OF_YEAR).apply { println(this) }
        now.get(ChronoField.DAY_OF_MONTH).apply { println(this) }
        now.get(ChronoField.HOUR_OF_DAY).apply { println(this) }
        now.get(ChronoField.MINUTE_OF_HOUR).apply { println(this) }
        now.get(ChronoField.SECOND_OF_MINUTE).apply { println(this) }
        now.get(ChronoField.MILLI_OF_SECOND).apply { println(this) }

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