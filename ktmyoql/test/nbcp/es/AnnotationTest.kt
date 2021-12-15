package nbcp.es

import nbcp.TestBase
import nbcp.comm.*
import nbcp.db.DbEntityIndex
import nbcp.db.DbEntityIndexes
import org.junit.jupiter.api.Test

@DbEntityIndexes(
    DbEntityIndex("a", "dfdf", unique = true),
    DbEntityIndex(value = ["b"]),
)
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