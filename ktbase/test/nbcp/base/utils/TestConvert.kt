package nbcp.base.utils

import nbcp.base.TestBase
import nbcp.base.comm.*
import nbcp.base.comm.*;
import nbcp.base.db.*;
import nbcp.base.enums.*;
import nbcp.base.extend.*;
import nbcp.base.utils.*;
import org.junit.jupiter.api.Test

class TestUtils : TestBase() {
    @Test
    fun testjs() {
        println(ScriptLanguageEnum.js.info())
        var d = "(function(a,b){ return a+b;})(1,3)"
        println(JsUtil.execScript(d))

    }


    @Test
    fun abc() {
        val str = """MyUtil.getKebabCase("abcDefa-fw")"""


        println("df".Slice(0, 100))
    }
}