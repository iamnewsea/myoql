package nbcp.model

import nbcp.TestBase
import org.junit.jupiter.api.Test


class MasterAlternateMapTest : TestBase() {
    @Test
    fun abc() {
        val m = MasterAlternateStack<String>({
            println(it)
        })

        Thread.sleep(3000)
    }
}