package nbcp.mongo

import nbcp.TestBase
import nbcp.comm.*
import nbcp.db.IdName
import nbcp.db.db
import nbcp.db.mongo.entity.SysAnnex
import nbcp.utils.CodeUtil
import org.bson.Document
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach

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