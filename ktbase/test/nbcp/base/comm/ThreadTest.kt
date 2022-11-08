package nbcp.base.comm

import nbcp.base.TestBase
import org.junit.jupiter.api.Test
import kotlin.concurrent.thread

class ThreadTest : TestBase() {

    @Test
    fun test_thread() {
        var result = thread(start = false) {
            while (true) {
                Thread.sleep(3000)
            }
        }

        println(result.isAlive)
        if (result.isAlive == false) {
            result.start()
        }
        println(result.isAlive)
    }
}