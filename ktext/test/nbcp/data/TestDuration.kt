package nbcp.data

import nbcp.TestBase
import org.junit.Test
import java.time.Duration

class TestDuration : TestBase() {
    @Test
    fun Test_Duration(){
        var d = Duration.parse("PT20M");
        println(d.seconds)
    }
}