package nbcp.base.utils

import nbcp.base.TestBase
import nbcp.base.comm.*;
import nbcp.base.db.*;
import nbcp.base.enums.*;
import nbcp.base.extend.*;
import nbcp.base.utils.*;
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