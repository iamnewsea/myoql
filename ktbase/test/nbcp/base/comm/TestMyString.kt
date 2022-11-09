package nbcp.base.comm

import nbcp.base.TestBase
import nbcp.base.extend.ToJson
import nbcp.base.extend.toUtf8String
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream

class TestMyString : TestBase() {


    @Test
    fun test_get_json() {
        var map1 = mapOf("a" to 1, "b" to 2)
        var map2 = mapOf("a" to 1, "b" to 3)
        var map3: Map<String, Int>? = null;

        println((map1 + map2).ToJson())   // b=3
        println((map2 + map1).ToJson())   // b=2

    }


    @Test
    fun test_usefff() {
        println("abc".toByteArray().toUtf8String())
    }

    fun fun_1() {
        ByteArrayOutputStream().use {

            while (true) {
                ByteArrayOutputStream().use {
                    return
                }
            }


            throw RuntimeException("OK")
        }
    }
}