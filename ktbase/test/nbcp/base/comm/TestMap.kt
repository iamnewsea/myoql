package nbcp.base.comm

import nbcp.*
import nbcp.base.TestBase
import org.junit.jupiter.api.Test
import nbcp.base.comm.*;
import nbcp.base.db.*;
import nbcp.base.enums.*;
import nbcp.base.extend.*;
import nbcp.base.utils.*;

class TestMap : TestBase() {


    @Test
    fun test_Map() {
        var map = JsonMap()
        map.setValueByWbsPath("a1.b2.a",value = "a")
        map.setValueByWbsPath("a1.b2.b",value = "b")
        map.setValueByWbsPath("a1.b2.c",value = "c")
        map.setValueByWbsPath("a1.b2.d.e",value = "e")

        println(map.ToJson())

        map.removeByWbsPath("a1.b2.d.e")
        println(map.ToJson())


    }

}