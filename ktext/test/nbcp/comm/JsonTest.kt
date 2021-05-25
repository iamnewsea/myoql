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
        var a = abc("udi")
        a.r = MyRawString("<abc?");

        usingScope(JsonStyleEnumScope.DateUtcStyle) {
            println(a.ToJson())

            println(a.ToJson().FromJson<abc>()!!.r)
        }
    }
}