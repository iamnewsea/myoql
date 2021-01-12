package nbcp.utils

import nbcp.TestBase
import nbcp.comm.*
import nbcp.helper.ScriptLanguageEnum
import org.junit.Test
import java.time.format.DateTimeFormatter

class TestUtils : TestBase() {
    @Test
    fun testjs() {
        println(ScriptLanguageEnum.js.info())
        var d = "(function(a,b){ return a+b;})(1,3)"
        println(JsUtil.execScript(d))

    }


    @Test
    fun abc() {
        var str = """MyUtil.getKebabCase("abcDefa-fw")"""


        println("df".Slice(0, 100))
    }
}