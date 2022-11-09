package nbcp.base.utils

import nbcp.base.TestBase
import nbcp.base.comm.JsonMap
import nbcp.base.db.IdName
import nbcp.base.extend.AllGetPropertyMethods
import nbcp.base.extend.GetActualClass
import org.junit.jupiter.api.Test
import java.lang.reflect.ParameterizedType

class ReflectUtils : TestBase() {
    var list = listOf<IdName>();

    @Test
    fun r() {
        var type = this::class.java;
        var field = type.getDeclaredField("list");
        println((field.genericType as ParameterizedType).GetActualClass(0).simpleName)
    }

    @Test
    fun r2() {
        ClassUtil.getClasses("nbcp").forEach {
            println(it)
        }
    }

    class abc : JsonMap() {
        var ddd: String
            get() {
                return ""
            }
            set(value) {}
    }

    @Test
    fun testAllGetPropertyMethods() {
        println(abc::class.java.AllGetPropertyMethods)
    }
}