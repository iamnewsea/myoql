package nbcp

import nbcp.TestBase
import org.junit.jupiter.api.Test
import nbcp.comm.*
import nbcp.db.sql.entity.*
import java.io.File


class tool : TestBase() {

    @Test
    fun gen_dbr() {
        var path = Thread.currentThread().contextClassLoader.getResource("").path.split("/target/")[0];

        nbcp.db.mysql.tool.generator().work(
            File(path).parentFile.path + "/ktmyoql/kotlin/nbcp/db/sql/dbr_base_tables.kt",
            "nbcp.db.sql.entity.",
            s_city::class.java
        )
    }

    @Test
    fun gen_mor() {
        val path = Thread.currentThread().contextClassLoader.getResource("").path.split("/target/")[0]

        nbcp.db.mongo.tool.generator().work(
            File(path).parentFile.path + "/ktmyoql/kotlin/nbcp/db/mongo/mor_base_tables.kt",
            "nbcp.db.mongo.entity.",
            s_city::class.java,
            arrayOf(),
            StringMap(), listOf()
        )
    }


}