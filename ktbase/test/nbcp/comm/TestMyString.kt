package nbcp.comm

import nbcp.TestBase
import nbcp.db.IdName
import nbcp.scope.JsonSceneEnumScope
import nbcp.utils.RecursionUtil
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.Serializable
import java.time.LocalDateTime

class TestMyString : TestBase() {


    @Test
    fun test_get_json() {
        var d = MyString("ok")
        var d2 = d.CloneObject();
        println(d2)
    }


    @Test
    fun test_usefff() {
        println(fun_1())
    }

    fun fun_1() {
        ByteArrayOutputStream().use {

            while(true) {
                ByteArrayOutputStream().use {
                    return
                }
            }


            throw RuntimeException("OK")
        }
    }
}