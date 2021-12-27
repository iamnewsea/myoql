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
import java.time.LocalDateTime
import java.util.*

class Test_Mongo_Query : TestBase() {


    @Test
    fun test_query_datetime() {
        var start = LocalDateTime.now().minusHours(1)
        var end = LocalDateTime.now().plusHours(1);
        db.mor_base.sysLog.query()
            .where { it.createAt match_between (start to end) }
            .toList()
            .forEach {
                println(it.ToJson())
            }
    }

    @Test
    fun testCond() {
        usingScope(LogLevelScope.info) {
            db.mor_base.sysAnnex.aggregate()
                .addPipeLine(
                    PipeLineEnum.addFields,
                    db.mongo.cond(db.mor_base.sysAnnex.group match "digitalthread", "1", "0").As("u")
                )
                .beginMatch()
                .where { it.ext match "png" }
                .endMatch()
                .addPipeLine(PipeLineEnum.sort, JsonMap("u" to 1))
                .limit(0, 2)
                .toList()
                .forEach {
                    println(it.ToJson())
                }

        }
    }
}