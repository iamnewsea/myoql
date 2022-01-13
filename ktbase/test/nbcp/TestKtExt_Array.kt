package nbcp

import ch.qos.logback.classic.Level
import nbcp.comm.*
import org.junit.jupiter.api.Test
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