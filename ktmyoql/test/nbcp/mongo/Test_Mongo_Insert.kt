package nbcp.mongo

import ch.qos.logback.classic.Level
import nbcp.TestBase
import nbcp.comm.*
import nbcp.db.IdName
import nbcp.db.db
import nbcp.db.mongo.*
import nbcp.db.mongo.entity.BasicUser
import nbcp.db.mongo.entity.SysAnnex
import nbcp.db.mongo.entity.SysLog
import nbcp.db.sql.doInsert
import nbcp.db.sql.entity.s_annex
import nbcp.db.sql.updateWithEntity
import nbcp.tool.UserCodeGenerator
import nbcp.utils.CodeUtil
import org.bson.Document
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.springframework.transaction.annotation.Transactional
import java.lang.RuntimeException

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
        db.mor_base.sysAnnex.doInsert(annex);

    }

    private fun test_doc() {
        var annex = Document()
        annex.put("_id", CodeUtil.getCode())
        annex.put("name", "test_doc")
        annex.put("creator", JsonMap("id" to CodeUtil.getCode(), "name" to "test"))
        db.mor_base.sysAnnex.doInsert(annex);
    }
}