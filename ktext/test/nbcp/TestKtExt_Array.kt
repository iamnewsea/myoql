package nbcp

import ch.qos.logback.classic.Level
import nbcp.comm.*
import nbcp.utils.*
import nbcp.comm.*
import nbcp.utils.*
import nbcp.db.IdName
import nbcp.db.IdUrl
import org.junit.jupiter.api.Test
import org.springframework.stereotype.Service
import java.io.BufferedReader
import java.io.InputStreamReader
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class TestKtExt_Array : TestBase() {

    @Test
    fun test_groupBy() {
        var list = mutableListOf<Array<String>>();
        list.add(arrayOf("abc","e"))
        list.add(arrayOf("a","efff","ww"))
        list.add(arrayOf("wf","ewwfwf"))

        var group = list.groupBy { it.size }.maxByOrNull { it.key }?.value?.firstOrNull()
        println(group.ToJson())
    }
}