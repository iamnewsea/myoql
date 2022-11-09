package nbcp.myoql.es

import nbcp.myoql.TestBase
import nbcp.base.db.DbEntityIndex
import nbcp.base.db.DbEntityIndexes
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