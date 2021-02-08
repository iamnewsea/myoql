package nbcp.sql

import ch.qos.logback.classic.Level
import com.zaxxer.hikari.HikariDataSource
import nbcp.TestBase
import nbcp.comm.*
import nbcp.db.IdName
import nbcp.db.db
import nbcp.db.mysql.tool.MysqlEntityGenerator
import nbcp.db.sql.doInsert
import nbcp.db.sql.entity.s_annex
import nbcp.db.sql.updateWithEntity
import nbcp.tool.UserCodeGenerator
import nbcp.utils.SpringUtil
import org.junit.Test
import org.junit.jupiter.api.BeforeEach
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.jdbc.core.JdbcTemplate
import javax.sql.DataSource

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
        usingScope(LogScope.debug) {
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

    @Test
    fun test_ds() {
        var ds_main = SpringUtil.getBean<DataSource>() as HikariDataSource

        println(ds_main.maximumPoolSize)
    }
    @Test
    fun test_gen2() {
        var file = "abc (def)"

        println(Regex("""(\s*\(\s*[\w-_]+\s*\)\s*)""").replace(file, ""))
    }
    @Test
    fun test_gen() {
        var file = UserCodeGenerator.genVueCard("base", db.sql_base.s_annex);

        println(file)
    }
}