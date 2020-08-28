package nbcp.data

import nbcp.TestBase
import org.junit.Test
import java.time.Duration

class TestDuration : TestBase() {
    @Test
    fun Test_Duration(){
        var d = Duration.parse("P3D");
        println(d.seconds)
    }
}