package nbcp.es

import nbcp.TestBase
import nbcp.comm.*
import nbcp.db.DbDefine
import nbcp.db.DbDefines
import nbcp.utils.CookieData
import org.junit.jupiter.api.Test
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter


class AnnotationTest : TestBase() {

    @DbDefines(
        DbDefine("f1", "v1"),
        DbDefine("f2", "v2"),
        DbDefine("f3", "v3")
    )
    class abc {

    }

    @Test
    fun test_time() {
        var d = abc::class.java.getAnnotation(DbDefines::class.java);
        d.value.forEach { println(it.fieldName) }
    }
}