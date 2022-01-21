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
import java.lang.reflect.Proxy
import java.time.LocalDateTime
import java.util.*

class TestKtExt_Mongo : TestBase() {
    @BeforeEach
    fun init() {

    }

    @Test
    fun test_Where() {

        var query = db.mor_base.basicUserLoginInfo.query()
            .where { it.loginName match "abc" }
            .where { it.email match_in arrayOf("ff", "ww") }
            .whereOr({ it.mobile match "dd" }, { it.isLocked match false })

        var where = query.whereData;
        println(where.ToJson())
    }


    @Test
    fun genCode() {
        val d = UserCodeGenerator.genVueCard("MongoBase", db.mor_base.basicUserLoginInfo)
        println(d)
    }
}