package nbcp.es

import nbcp.TestBase
import nbcp.comm.*
import nbcp.db.DbDefine
import nbcp.db.DbDefines
import nbcp.db.DbEntityIndex
import nbcp.db.DbEntityIndexes
import nbcp.utils.CookieData
import org.junit.jupiter.api.Test
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter


@DbEntityIndex("a", "dfdf", unique = true)
@DbEntityIndex(value = ["b"])
@DbEntityIndex("c")
class AnnotationTest : TestBase() {


    class abc {

    }

    @Test
    fun test_time() {
        var d = AnnotationTest::class.java.getAnnotationsByType(DbEntityIndex::class.java);
        d.forEach { println(it.value.joinToString(",")) }
    }
}