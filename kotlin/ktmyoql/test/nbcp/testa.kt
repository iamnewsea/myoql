package nbcp

import nbcp.db.db
import org.junit.Test

class testa : TestBase() {
    @Test
    fun abc() {
        db.affectRowCount = 0;
        println(db.affectRowCount)
    }
}