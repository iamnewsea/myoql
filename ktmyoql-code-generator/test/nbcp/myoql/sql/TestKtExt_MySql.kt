package nbcp.myoql.sql

import com.zaxxer.hikari.HikariDataSource
import nbcp.base.db.IdName
import nbcp.base.enums.LogLevelScopeEnum
import nbcp.base.extend.*
import nbcp.base.extend.usingScope
import nbcp.base.utils.SpringUtil
import nbcp.base.utils.StringUtil
import nbcp.myoql.TestBase
import nbcp.myoql.code.generator.db.mysql.MysqlEntityGenerator
import nbcp.myoql.code.generator.tool.CrudCodeGeneratorUtil
import nbcp.myoql.db.db
import nbcp.myoql.db.sql.component.WhereData
import nbcp.myoql.db.sql.component.doInsert
import nbcp.myoql.db.sql.component.updateWithEntity
import nbcp.myoql.db.sql.entity.s_annex
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.FileWriter
import javax.sql.DataSource

class TestKtExt_MySql : TestBase() {
    @BeforeEach
    fun init() {

    }

    @Test
    fun test_Insert_ConverterValueToDb() {
        var file = s_annex();
        file.creator = IdName("1", "abc");
        file.name = "OK";

        db.sqlBase.s_annex.doInsert(file)

        println()
    }

    @Test
    fun test_update_ConverterValueToDb() {
        db.sqlBase.s_annex.updateById("56Fgk7UEAm0Z")
            .set { it.id to "56Fgk7UEAm0Z" }
            .exec();

        println()
    }

    @Test
    fun test_update_spread() {
        usingScope(LogLevelScopeEnum.DEBUG) {
            var ent = db.sqlBase.s_annex.queryById("56Fgk7UEAm0Z").toEntity()!!;
            ent.creator = IdName("2", "rr")
            db.sqlBase.s_annex.updateWithEntity(ent)
                .set { it.name to "eee" }
                .exec();

            println()
        }
    }

    @Test
    fun test_select_spread() {
        var ent = db.sqlBase.s_annex.queryById("56Fgk7UEAm0Z")
            .apply {
                var where = WhereData();
                where.or(db.sqlBase.s_annex.ext sqlEquals "png")
                where.or(db.sqlBase.s_annex.ext sqlEquals "gif")
                where.or(db.sqlBase.s_annex.ext sqlEquals "jpg")
                where.or(db.sqlBase.s_annex.ext sqlEquals "bmp")
                this.where { where }
            }
            .toSql();

        println(ent.ToJson())
        println(ent.values.ToJson())
    }

    @Test
    fun test_ds() {
        var ds_main = SpringUtil.getBean<DataSource>() as HikariDataSource

        println(ds_main.maximumPoolSize)
    }

    @Test
    fun test_gen2() {
        var file = "abc (def)"

        println(Regex("""\s*\(\s*[\w-_]+\s*\)\s*""").replace(file, ""))

        println(Regex(Regex("""\s*\(\s*(auto_id|auto_number)\s*\)\s*""").replace("abc (auto_number) (dd)", "")))
    }

    @Test
    fun test_gen() {
        val file = CrudCodeGeneratorUtil.genVueCard("base", db.sqlBase.s_annex);

        println(file)
    }

    @Test
    fun test_jpa() {
        MysqlEntityGenerator.db2Entity().toJpaCode("com.kjwt.gis.entity").forEach {
            FileWriter("/opt/udi/" + StringUtil.getBigCamelCase(it.id) + ".java").use { f ->
                f.write(it.name);
            }
        }
    }

    @Test
    fun test_mybatis() {
        MysqlEntityGenerator.db2Entity().toJpaCode("com.kjwt.gis.entity").forEach {
            FileWriter("d:\\ent\\" + StringUtil.getBigCamelCase(it.id) + ".java").use { f ->
                f.write(it.name);
            }
        }
    }

}