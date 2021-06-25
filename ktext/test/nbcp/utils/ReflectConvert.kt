package nbcp.utils

import nbcp.TestBase
import nbcp.comm.*
import nbcp.db.IdName
import nbcp.helper.ScriptLanguageEnum
import org.junit.Test
import java.lang.reflect.ParameterizedType
import java.time.format.DateTimeFormatter

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
}