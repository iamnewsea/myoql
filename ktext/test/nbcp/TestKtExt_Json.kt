package nbcp

import ch.qos.logback.classic.Level
import nbcp.comm.*
import nbcp.utils.*
import nbcp.comm.*
import nbcp.utils.*
import nbcp.db.IdName
import nbcp.db.IdUrl
import org.junit.Test
import org.springframework.stereotype.Service
import java.io.BufferedReader
import java.io.InputStreamReader
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class TestKtExt_Json : TestBase() {

    class bc {
        var isDeleted:Boolean? = false;
    }

    @Test
    fun test_ToJson() {
        var b = bc();
        b.isDeleted = true;

        using(JsonStyleEnumScope.GetSetStyle) {
            println(b.ToJson())
        }

        using(JsonStyleEnumScope.FieldStyle) {
            println(b.ToJson())
        }
    }

    @Test
    fun test_FromJson() {
        using(JsonStyleEnumScope.GetSetStyle) {
            println("""{"isDeleted":true}""".FromJson<bc>()!!.isDeleted)
        }

        using(JsonStyleEnumScope.FieldStyle) {
            println("""{"isDeleted":true}""".FromJson<bc>()!!.isDeleted)
        }
    }
}