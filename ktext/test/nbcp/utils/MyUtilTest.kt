package nbcp.utils

import nbcp.TestBase
import nbcp.comm.StringMap
import org.junit.Test

class MyUtilTest : TestBase() {
    @Test
    fun test1() {
        println(MyUtil.getBigCamelCase("abc--d__efnf"))
    }

    @Test
    fun testformat() {
        var ret = MyUtil.formatTemplateJson("dbr.\${group|w}.\${entity}.queryById(id)",
            StringMap("group" to "wx", "entity" to "user"), {key, value,func, param ->
                ""
            })

        println(ret)
    }
}