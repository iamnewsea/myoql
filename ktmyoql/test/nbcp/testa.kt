package nbcp

import nbcp.db.DbEntityFieldRef
import nbcp.db.DbEntityFieldRefs
import nbcp.db.db
import org.junit.Test

class testa : TestBase() {

    @DbEntityFieldRefs(arrayOf(DbEntityFieldRef("id", "name", testa::class, "id", "name"),
            DbEntityFieldRef("id2", "name2", testa::class, "id2", "name2")
    ))
    class abcd {

    }

    @Test
    fun abc() {
        var d = abcd();
        var es = d::class.java.annotations
        var df = 1;
    }
}