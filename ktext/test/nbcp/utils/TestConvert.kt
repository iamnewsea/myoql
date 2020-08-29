package nbcp.utils

import nbcp.TestBase
import nbcp.comm.*
import org.junit.Test
import java.time.format.DateTimeFormatter

class TestUtils : TestBase() {
    @Test
    fun testjs() {
        var d = "(function(a,b){ return a+b;})(1,3)"
        println(JsUtil.execScript(d))
    }


    @Test
    fun abc(){
        //error
        //println("df".substring(0,100))

        println("df".Slice(0,100))
    }
}