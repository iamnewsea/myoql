package nbcp.base

import nbcp.TestBase
import nbcp.comm.JsonMap
import org.junit.Test
import java.time.Duration

class TestDuration : TestBase() {
    @Test
    fun Test_Duration() {
        var d = Duration.parse("PT20M");
        println(d.seconds)
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