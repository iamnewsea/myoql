package nbcp.base.comm

import nbcp.base.TestBase
import nbcp.base.extend.ToJson
import nbcp.base.extend.removeByWbsPath
import nbcp.base.extend.setValueByWbsPath
import org.junit.jupiter.api.Test

class TestMap : TestBase() {


    @Test
    fun test_Map() {
        var map = JsonMap()
        map.setValueByWbsPath("a1.b2.a", value = "a")
        map.setValueByWbsPath("a1.b2.b", value = "b")
        map.setValueByWbsPath("a1.b2.c", value = "c")
        map.setValueByWbsPath("a1.b2.d.e", value = "e")

        println(map.ToJson())

        map.removeByWbsPath("a1.b2.d.e")
        println(map.ToJson())


    }

}