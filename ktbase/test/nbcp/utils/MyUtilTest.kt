package nbcp.utils

import nbcp.TestBase
import nbcp.comm.JsonMap
import nbcp.comm.StringMap
import nbcp.comm.ToJson
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
    fun testGetValue() {
        val json = JsonMap("a" to 1, "b" to JsonMap("c" to 2), "d" to IdName("id1", "name1"))

        println(MyUtil.getValueByWbsPath(json, "a"))
        println(MyUtil.getValueByWbsPath(json, "b.c"))
        println(MyUtil.getValueByWbsPath(json, "d.id"))
        println(MyUtil.getValueByWbsPath(json, "e[0]"))
        println(json.ToJson())
    }

    @Test
    fun setValue() {
        val json = JsonMap()
        MyUtil.setValueByWbsPath(json, "a[3]", ignoreCase = true, value = 1)
        println(json.ToJson())
        MyUtil.setValueByWbsPath(json, "b[0].c", ignoreCase = true, value = 2)
        println(json.ToJson())
        MyUtil.setValueByWbsPath(json, "d.e[3].id", ignoreCase = true, value = 3)
        println(json.ToJson())
    }
}