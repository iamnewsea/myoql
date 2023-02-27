package nbcp.myoql

import nbcp.myoql.code.generator.db.mongo.MorGenerator4Kotlin
import nbcp.myoql.code.generator.db.mysql.DbrGenerator4Kotlin
import org.junit.jupiter.api.Test
import java.io.File


class tool : TestBase() {

    @Test
    fun gen_dbr() {
        var path = System.getProperty("user.dir");

        DbrGenerator4Kotlin()
            .work(
                File(path).parentFile.path + "/ktmyoql/kotlin",
                "nbcp.myoql.db.sql.entity.",
                "nbcp.myoql.db.sql.table"
            )
    }

    @Test
    fun gen_mor() {
        val path = System.getProperty("user.dir")

        MorGenerator4Kotlin()
            .work(
                File(path).parentFile.path + "/ktmyoql/kotlin",
                "nbcp.myoql.db.mongo.entity.",
                "nbcp.myoql.db.mongo.table",
                ignoreGroups = listOf()
            )
    }


}