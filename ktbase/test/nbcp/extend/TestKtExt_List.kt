package nbcp.extend

import nbcp.TestBase
import nbcp.comm.*
import nbcp.utils.*
import org.junit.jupiter.api.Test
import java.util.regex.Pattern

class TestKtExt_List : TestBase() {


    @Test
    fun test_cn() {
        var list = listOf("ok", "ab", "xy", "m", "n")

        println(list.Slice(1, 2).ToJson())
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