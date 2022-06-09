package nbcp.utils

import nbcp.TestBase
import nbcp.comm.*
import nbcp.component.SnowFlake
import org.junit.jupiter.api.Test
import java.io.File
import java.time.format.DateTimeFormatter

class ClassTest : TestBase() {
    @Test
    fun test_code() {
        var d = 0;
        println(Int::class.java.simpleName)
        println(Float::class.java.simpleName)
        println(Int::class.java.IsType("Int"))
        println(Float::class.java.IsType("Float"))
    }
}