package nbcp.utils

import nbcp.TestBase
import org.junit.Test

class MyUtilTest:TestBase() {
    @Test
    fun test1(){
        println(MyUtil.getBigCamelCase("abc--d__efnf"))
    }
}