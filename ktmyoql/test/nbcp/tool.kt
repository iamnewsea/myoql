package nbcp

import nbcp.TestBase
import org.junit.Test
import nbcp.comm.*
import nbcp.comm.*
import nbcp.base.extend.*
import nbcp.db.mongo.SysAnnex
import nbcp.db.mysql.s_annex
import java.io.File
import java.lang.reflect.ParameterizedType
import kotlin.reflect.full.memberProperties


class tool : TestBase() {

    @Test
    fun gen_dbr() {
        var path = Thread.currentThread().contextClassLoader.getResource("").path.split("/target/")[0];

        nbcp.db.mysql.tool.generator().work(
                File(path).parentFile.path + "/ktmyoql/kotlin/nbcp/db/mysql/dbr_tables.kt",
                "nbcp.db.mysql.",
                s_annex::class.java
        )
    }

    @Test
    fun gen_mor() {
        var path = Thread.currentThread().contextClassLoader.getResource("").path.split("/target/")[0]

        nbcp.db.mongo.tool.generator().work(
                File(path).parentFile.path + "/ktmyoql/kotlin/nbcp/db/mongo/mor_tables.kt",
                "nbcp.db.mongo.",
                SysAnnex::class.java
        )
    }
}