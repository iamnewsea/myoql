package nbcp.comm

import nbcp.TestBase
import nbcp.db.IdUrl
import org.junit.Test
import java.time.Duration

class JsonTest : TestBase() {

    data class abc(var name: String) {
        val fullName: String
            get() {
                return this.name + "!"
            }

        var r: MyRawString = MyRawString();
    }

    @Test
    fun test_get_json() {
        var cls = mutableListOf<String>()::class.java
        println()
    }
}