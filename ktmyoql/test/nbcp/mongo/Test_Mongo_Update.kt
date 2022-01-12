package nbcp.mongo

import ch.qos.logback.classic.Level
import nbcp.TestBase
import nbcp.comm.*
import nbcp.db.DbIncData
import nbcp.db.IdName
import nbcp.db.db
import nbcp.db.mongo.*
import nbcp.db.mongo.entity.BasicUser
import nbcp.db.mongo.entity.SysAnnex
import nbcp.db.mongo.entity.SysLog
import nbcp.db.op_inc
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
        var d = db.mor_base.sysLastSortNumber.update()
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
        var d = db.mor_base.sysAnnex.updateWithEntity(annex)
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

        var annex2 = Document()
        annex2.put("id", CodeUtil.getCode())
        annex2.put("name", "test_doc")
        annex2.put("creator", JsonMap("id" to CodeUtil.getCode(), "name" to "test"))
        var update = db.mor_base.sysAnnex.updateWithEntity(annex).withRequestJson(annex2).castToUpdate();

        println(db.mongo.getMergedMongoCriteria(update.whereData).toDocument().toJson())
        println(update.setData.ToJson())
    }
}