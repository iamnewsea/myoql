package nbcp.base.utils

import nbcp.base.TestBase
import nbcp.base.enums.ScriptLanguageEnum
import nbcp.base.extend.Slice
import org.junit.jupiter.api.Test

class TestUtils : TestBase() {
    @Test
    fun testjs() {
        println(ScriptLanguageEnum.JAVA_SCRIPT.info())
        var d = "(function(a,b){ return a+b;})(1,3)"
        println(JsUtil.execScript(d))

    }


    @Test
    fun abc() {
        val str = """MyUtil.getKebabCase("abcDefa-fw")"""


        println("df".Slice(0, 100))
    }
}