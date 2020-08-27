package nbcp.utils

import nbcp.TestBase
import nbcp.comm.*
import nbcp.component.SnowFlake
import org.junit.Test
import java.time.format.DateTimeFormatter

class CodeTest : TestBase() {
    @Test
    fun test_code() {
        var d = CodeUtil.getCode();

        println(CodeUtil.getDateTimeFromCode(d).AsString())
    }
}