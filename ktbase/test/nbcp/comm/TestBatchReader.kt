package nbcp.comm

import nbcp.*
import nbcp.db.*
import org.junit.jupiter.api.Test
import nbcp.scope.*

class TestBatchReader : TestBase() {


    @Test
    fun test_BatchReader() {
        BatchReader.init { skip, take ->
            if (skip >= 1000) {
                return@init listOf()
            }
            var list = mutableListOf<Int>()
            for (i in 1..5) {
                list.add(skip + i)
            }
            list;
        }.apply {
            this.forEach {
                println(it)
            }
        }


    }

}