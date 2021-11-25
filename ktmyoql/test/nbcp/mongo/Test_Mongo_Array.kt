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

class Test_Mongo_Array : TestBase() {
    @BeforeEach
    fun init() {

    }

    @Test
    fun Pop_Where() {
//        db.mor_base.sysAnnex.update()
//            .where { it.corpId match "abc" }
//            .pull { it.tags, "" }
    }
}