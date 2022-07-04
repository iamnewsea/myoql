package nbcp.extend

import nbcp.TestBase
import nbcp.comm.*
import nbcp.utils.*
import org.junit.jupiter.api.Test
import java.util.regex.Pattern

class TestKtExt_Map : TestBase() {


    @Test
    fun test_cn() {
        var mapb = mapOf("b" to 1)
        var mapc = mapOf("c" to 2)

        var map1 = mapOf("a" to mapb, "x" to 10)
        var map2 = mapOf("a" to mapc, "y" to 11)

        println(map1.deepJoin(map2).ToJson())
    }
}