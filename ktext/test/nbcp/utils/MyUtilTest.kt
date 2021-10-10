package nbcp.utils

import nbcp.TestBase
import nbcp.comm.JsonMap
import nbcp.comm.StringMap
import nbcp.db.IdName
import org.junit.jupiter.api.Test

class MyUtilTest : TestBase() {
    @Test
    fun test1() {
        println(MyUtil.getBigCamelCase("abc--d__efnf"))
    }

    @Test
    fun getMethod() {
        println(
            MyUtil.getCurrentMethodInfo().methodName
        )
    }

    @Test
    fun getCenterLines() {
        val txt = """
================================================
TomcatWebServer:8002 -- nacos: saas-dev.nancal.com:8848(yuxh) -- nacos-config:none -- admin-api:yuxh
================================================
         """
        println(MyUtil.getCenterEachLine(txt.split("\n")).joinToString("\n"))
    }

    @Test
    fun testformat() {
        val ret = MyUtil.formatTemplateJson("dbr.\${group|w}.\${entity}.queryById(id)",
            StringMap("group" to "wx", "entity" to "user"), { key, value, func, param ->
                ""
            })

        println(ret)
    }

    @Test
    fun testformat2() {
        val json = JsonMap("a" to 1, "b" to JsonMap("c" to 2), "d" to IdName("id1", "name1"))

        println(MyUtil.getPathValue(json, "a"))
        println(MyUtil.getPathValue(json, "b.c"))
        println(MyUtil.getPathValue(json, "d.id"))
    }
}