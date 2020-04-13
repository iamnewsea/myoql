package nbcp

import nbcp.comm.Define
import nbcp.db.*
import nbcp.db.es.IEsDocument
import nbcp.db.es.tool.generator_mapping
import org.junit.Test

@DbEntityGroup("sys")
class e_test: IEsDocument() {
    @Define("")
    var name: String = ""
    var value: Int = 0
    var user: IdName = IdName()
    var corp: IdName = IdName()
}

class testa : TestBase() {
    @Test
    fun abc22() {

    }

    @Test
    fun abc() {
        var m = generator_mapping();
        m.work("mapping","nbcp",e_test::class.java)
    }
}