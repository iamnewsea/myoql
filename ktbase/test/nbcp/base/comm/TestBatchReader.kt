package nbcp.base.comm

import nbcp.base.TestBase
import org.junit.jupiter.api.Test

class TestBatchReader : TestBase() {


    @Test
    fun test_BatchReader() {
        nbcp.base.comm.BatchReader.init { skip, take ->
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