package nbcp.sql

import ch.qos.logback.classic.Level
import nbcp.TestBase
import nbcp.comm.*
import nbcp.db.IdName
import nbcp.db.db
import nbcp.db.sql.doInsert
import nbcp.db.sql.entity.s_annex
import nbcp.db.sql.updateWithEntity
import org.junit.Test
import org.junit.jupiter.api.BeforeEach

class TestKtExt_MySql : TestBase() {
    @BeforeEach
    fun init() {

    }

    @Test
    fun test_Insert_ConverterValueToDb() {
        var file = s_annex();
        file.id = "56Fgk7UEAm0w"
        file.creator = IdName("1", "abc");
        file.name = "OK";

        db.sql_base.s_annex.doInsert(file)

        println()
    }

    @Test
    fun test_update_ConverterValueToDb() {
        db.sql_base.s_annex.updateById("56fgk7ueam0w")
                .set { it.id to "56Fgk7UEAm0Z" }
                .exec();

        println()
    }

    @Test
    fun test_update_spread() {
        usingScope(LogScope(Level.DEBUG_INT)) {
            var ent = db.sql_base.s_annex.queryById("56fgk7ueam0w").toEntity()!!;
            ent.creator = IdName("2", "rr")
            db.sql_base.s_annex.updateWithEntity(ent)
                    .set { it.name to "eee" }
                    .exec();

            println()
        }
    }

    @Test
    fun test_select_spread() {
        var ent = db.sql_base.s_annex.queryById("56fgk7ueam0w").toEntity()

        println(ent.ToJson())
    }
}