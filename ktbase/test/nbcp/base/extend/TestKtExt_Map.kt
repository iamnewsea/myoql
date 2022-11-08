package nbcp.base.extend

import nbcp.base.TestBase
import org.junit.jupiter.api.Test

class TestKtExt_Map : TestBase() {


    @Test
    fun test_cn() {
        var mapb = mapOf("b" to 1)
        var mapc = mapOf("c" to 2, "d" to null)

        var map1 = mapOf("a" to mapb, "x" to 10)
        var map2 = mapOf("a" to mapc, "y" to 11)

        println(map1.deepJoin(map2).ToJson())
    }


    @Test
    fun test_aaa() {
        var mapc = mapOf("c" to 2, "y" to "m")
        var mapb = mapOf("b" to listOf(mapc), "x" to 3)
        var map = mapOf("a" to mapb)


        println(map.ToListKv())
        println(map.getValueByWbsPath("a.b[0].c"))
    }
}