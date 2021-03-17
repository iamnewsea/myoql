package nbcp.model

import nbcp.TestBase
import org.junit.Test


class MasterAlternateMapTest : TestBase() {
    @Test
    fun abc() {
        var m = MasterAlternateStack<String>({
            println(it)
        })

        Thread.sleep(3000)
    }
}