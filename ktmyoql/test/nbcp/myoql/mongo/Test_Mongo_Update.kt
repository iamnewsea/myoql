package nbcp.myoql.mongo

import nbcp.myoql.TestBase
import nbcp.base.comm.*;
import nbcp.base.db.*;
import nbcp.base.extend.*;
import nbcp.base.utils.*;
import nbcp.myoql.db.mongo.*
import nbcp.db.mongo.entity.SysAnnex
import nbcp.myoql.db.comm.op_inc
import nbcp.myoql.db.db
import org.bson.Document
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach

class Test_Mongo_Update : TestBase() {
    @BeforeEach
    fun init() {

    }

    @Test
    fun TestUpdate() {
        test_doc();
        test_ent();
        test_save()
    }

    @Test
    fun test_save() {
        var d = db.morBase.sysLastSortNumber.update()
            .where { it.table match "abc" }
            .where { it.group match "def" }
            .inc { it.value op_inc 3 }
            .saveAndReturnNew();

        println(d.ToJson())
    }

    @Test
    fun test_ent() {
        var annex = SysAnnex()
        annex.name = "wwwfffw"
        annex.id = CodeUtil.getCode();
        annex.creator = IdName(CodeUtil.getCode(), "test")
        var d = db.morBase.sysAnnex.updateWithEntity(annex)
            .whereColumn { it.name }
            .castToUpdate()


        println(d)
    }

    @Test
    fun test_doc() {
        var annex = Document()
        annex.put("id", CodeUtil.getCode())
        annex.put("name", "test_doc")
        annex.put("name2", listOf("a", "b"))
        annex.put("creator", JsonMap("id" to CodeUtil.getCode(), "name" to "test"))

        var update = db.morBase.sysAnnex.updateWithEntity(annex)
            .whereColumn { it.id }
            .castToUpdate();

        update!!.exec();
    }
}