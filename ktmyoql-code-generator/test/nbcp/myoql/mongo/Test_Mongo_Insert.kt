package nbcp.myoql.mongo

import nbcp.base.comm.JsonMap
import nbcp.base.db.IdName
import nbcp.base.utils.CodeUtil
import nbcp.myoql.TestBase
import nbcp.myoql.db.db
import nbcp.myoql.db.mongo.entity.SysAnnex
import org.bson.Document
import org.bson.types.ObjectId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class Test_Mongo_Insert : TestBase() {
    @BeforeEach
    fun init() {

    }

    @Test
    fun TestInsert() {
        test_doc();
        test_ent();
    }

    private fun test_ent() {
        var annex = SysAnnex()
        annex.name = "test_ent"
        annex.id = CodeUtil.getCode();
        annex.creator = IdName(CodeUtil.getCode(), "test")
        db.morBase.sysAnnex.doInsert(annex);

    }

    @Test
    fun test_doc() {
        var annex = Document()
        annex.put("name", "test_doc")
        annex.put("creator", JsonMap("id" to ObjectId().toString(), "name" to "test"))
        db.morBase.sysAnnex.doInsert(annex);
    }
}