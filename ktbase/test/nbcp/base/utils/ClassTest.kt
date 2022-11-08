package nbcp.base.utils

import nbcp.base.TestBase
import nbcp.base.comm.*;
import nbcp.base.db.*;
import nbcp.base.enums.*;
import nbcp.base.extend.*;
import nbcp.base.utils.*;
import org.junit.jupiter.api.Test

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