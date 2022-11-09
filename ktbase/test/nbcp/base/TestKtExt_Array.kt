package nbcp.base

import nbcp.base.extend.ToJson
import org.junit.jupiter.api.Test


class TestKtExt_Array : TestBase() {

    @Test
    fun test_groupBy() {
        var list = mutableListOf<Array<String>>();
        list.add(arrayOf("abc", "e"))
        list.add(arrayOf("a", "efff", "ww"))
        list.add(arrayOf("wf", "ewwfwf"))

        var group = list.groupBy { it.size }.maxByOrNull { it.key }?.value?.firstOrNull()
        println(group.ToJson())
    }
}