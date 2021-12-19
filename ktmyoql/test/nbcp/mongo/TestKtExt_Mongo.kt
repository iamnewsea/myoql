package nbcp.mongo

import ch.qos.logback.classic.Level
import nbcp.TestBase
import nbcp.comm.*
import nbcp.db.IdName
import nbcp.db.db
import nbcp.db.mongo.*
import nbcp.db.mongo.entity.BasicUser
import nbcp.db.mongo.entity.SysLog
import nbcp.db.sql.doInsert
import nbcp.db.sql.entity.s_annex
import nbcp.db.sql.updateWithEntity
import nbcp.tool.UserCodeGenerator
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.springframework.transaction.annotation.Transactional
import java.lang.RuntimeException
import java.util.*

class TestKtExt_Mongo : TestBase() {
    @BeforeEach
    fun init() {

    }

    @Test
    fun test_Transactional() {
        var con = db.mongo.getMongoTemplateByUri("mongodb://dev:123@mongo:27017/cms")!!
        usingScope(MongoTemplateScope(con)) {
            var doc = JsonMap();
            doc.put("abc", UUID.randomUUID())
            db.mor_base.sysLog.doInsert(doc)
        }
    }

    @Test
    fun genCode() {
        val d = UserCodeGenerator.genVueCard("MongoBase", db.mor_base.basicUserLoginInfo)
        println(d)
    }

    @Test
    fun testCond() {
        usingScope(LogLevelScope.info) {
            db.mor_base.sysAnnex.aggregate()
                    .addPipeLine(PipeLineEnum.addFields,
                            db.mongo.cond(db.mor_base.sysAnnex.group match "digitalthread", "1", "0").As("u")
                    )
                    .addPipeLine(PipeLineEnum.sort, JsonMap("u" to 1))
                    .limit(0, 2)
                    .toList()
                    .forEach {
                        println(it.ToJson())
                    }

        }
    }
}