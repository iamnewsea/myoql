package nbcp.base.utils

import nbcp.base.TestBase
import nbcp.base.comm.JsonMap
import nbcp.base.comm.StringMap
import nbcp.base.db.IdName
import nbcp.base.extend.ToJson
import nbcp.base.extend.splitBoundary
import nbcp.base.extend.takeNumber
import org.junit.jupiter.api.Test

class MyUtilTest : TestBase() {
    @Test
    fun test1() {
        println(MyUtil.joinFilePath("/a", "..\\b", ".d/../c"))
        println(MyUtil.getRandomNumber(-1000, 9))
        println(MyUtil.getRandomNumber(9999, 10))
        println(MyUtil.getRandomWithLength(10))
        println(MyUtil.getBigCamelCase("abc--d__efnf"))
    }

    @Test
    fun getMethod() {
        println(CodeUtil.getCode())
        println(MyUtil.getCurrentMethodInfo().methodName)
    }

    @Test
    fun takeNumber12f() {
        println(Regex("(\\d+)").splitBoundary("111aaaa22222bbbb3333ccccc")).ToJson()
        println(Regex("(\\D)+").splitBoundary("111aaaa22222bbbb3333ccccc")).ToJson()
    }

    @Test
    fun takeNumber() {
        println("1a2b3c".takeNumber().ToJson())
        println("11111a222222b333333c".takeNumber().ToJson())
        println("~~~~~sdf```11111--------222222////////333333!!!!!!".takeNumber().ToJson())
    }

    @Test
    fun versionCompareTest() {
        println(MyUtil.compareVersion("1.10", "1.9"))
        println(MyUtil.compareVersion("1.10.s", "1.x"))
        println(MyUtil.compareVersion("1", "1.x"))
        println(MyUtil.compareVersion("1.10-SNAPSHOT", "1.9-SNAPSHOT"))
        println(MyUtil.compareVersion("1.10-SNAPSHOT", "1.10"))
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
        val ret = MyUtil.formatTemplateJson("dbr.\${group|-}.\${entity}.queryById(id)",
            StringMap("group" to "sysWx", "entity" to "user"), { key, value, func, param ->
                ""
            })

        println(ret)
    }

    @Test
    fun random() {
        println(MyUtil.getRandomWithLength(6))
    }

    @Test
    fun testGetValue() {
        val json = JsonMap("a" to 1, "b" to JsonMap("c" to 2), "d" to IdName("id1", "name1"))

        println(MyUtil.getValueByWbsPath(json, "a"))
        println(MyUtil.getValueByWbsPath(json, "b.c"))
        println(MyUtil.getValueByWbsPath(json, "d.id"))
        println(MyUtil.getValueByWbsPath(json, "e[0]"))
        println(MyUtil.getValueByWbsPath(json, "x.y.z", fillMap = true))
        println(json.ToJson())
    }

    @Test
    fun setValue() {
        val json = JsonMap()
        MyUtil.setValueByWbsPath(json, "a", ignoreCase = true, value = 1)
        println(json.ToJson())
        MyUtil.setValueByWbsPath(json, "b[0]", ignoreCase = true, value = 2)
        println(json.ToJson())
        MyUtil.setValueByWbsPath(json, "d.e[3].id", ignoreCase = true, value = 3)
        println(json.ToJson())
    }
}