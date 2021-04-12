package nbcp.comm

import nbcp.db.IdUrl
import org.junit.Test
import java.time.Duration

class JsonTest {

    data class abc(var name: String) {
        val fullName: String
            get() {
                return this.name + "!"
            }
    }

    @Test
    fun test_get_json() {
        var a = abc("udi")
        usingScope(JsonStyleEnumScope.GetSetStyle){
            println(a.ToJson())
        }
    }
}