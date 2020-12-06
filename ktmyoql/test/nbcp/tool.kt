package nbcp

import nbcp.TestBase
import org.junit.Test
import nbcp.comm.*
import nbcp.db.mongo.entity.*
import nbcp.db.sql.entity.*
import java.io.File
import java.lang.reflect.ParameterizedType
import kotlin.reflect.full.memberProperties


class tool : TestBase() {

    @Test
    fun gen_dbr() {
        var path = Thread.currentThread().contextClassLoader.getResource("").path.split("/target/")[0];

        nbcp.db.mysql.tool.generator().work(
                File(path).parentFile.path + "/ktmyoql/kotlin/nbcp/db/sql/dbr_base_tables.kt",
                "nbcp.db.sql.entity.",
                s_annex::class.java, StringMap(), listOf()
        )
    }

    @Test
    fun gen_mor() {
        var path = Thread.currentThread().contextClassLoader.getResource("").path.split("/target/")[0]

        nbcp.db.mongo.tool.generator().work(
                File(path).parentFile.path + "/ktmyoql/kotlin/nbcp/db/mongo/mor_base_tables.kt",
                "nbcp.db.mongo.entity.",
                SysAnnex::class.java,
                arrayOf(),
                StringMap(), listOf()
        )
    }


}