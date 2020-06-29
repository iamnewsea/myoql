package nbcp

import ch.qos.logback.classic.Level
import nbcp.comm.*
import nbcp.utils.*
import nbcp.comm.*
import nbcp.db.CityCodeName
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


    @Test
    fun test_FromJson() {
        using(JsonStyleEnumScope.GetSetStyle) {
            println("""{"code":123}""".FromJson<CityCodeName>()!!.name)
        }

        using(JsonStyleEnumScope.FieldStyle) {
            println("""{"code":123}""".FromJson<CityCodeName>()!!.name)
        }
    }
}