package nbcp.base

import nbcp.TestBase
import nbcp.comm.AsLocalDateTime
import nbcp.comm.JsonMap
import nbcp.comm.minus
import org.junit.Test
import java.time.Duration
import java.time.LocalDateTime

class TestDuration : TestBase() {
    @Test
    fun Test_Duration() {
        var d = Duration.parse("PT20M");
        println(d.seconds)
    }

    @Test
    fun Test_Duration2() {
        var d = LocalDateTime.now().minus("2020-9-14 12:00:00".AsLocalDateTime()!!);
        println(d.totalMinutes)
    }

    @Test
    fun b() {
        var j = JsonMap();
        for (i in 1..48) {
            j.put("k" + i, "value" + i)
        }
        println(j.get("k32"))
    }
}