package nbcp.myoql

import nbcp.myoql.TestBase
import nbcp.myoql.db.mongo.tool.MorGenerator4Kotlin
import nbcp.myoql.db.mysql.tool.DbrGenerator4Kotlin
import org.junit.jupiter.api.Test
import java.io.File


class tool : TestBase() {

    @Test
    fun gen_dbr() {
        var path = Thread.currentThread().contextClassLoader.getResource("").path.split("/target/")[0];

        DbrGenerator4Kotlin().work(
            File(path).parentFile.path + "/ktmyoql/kotlin",
            "nbcp.db.sql.entity."
        )
    }

    @Test
    fun gen_mor() {
        val path = Thread.currentThread().contextClassLoader.getResource("").path.split("/target/")[0]

        MorGenerator4Kotlin().work(
            File(path).parentFile.path + "/ktmyoql/kotlin",
            "nbcp.db.mongo.entity.",
            ignoreGroups = listOf()
        )
    }


}