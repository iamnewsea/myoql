package nbcp.base

import nbcp.TestBase
import nbcp.comm.GroupLog
import nbcp.comm.*
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.LocalDateTime
import kotlin.concurrent.thread

@GroupLog("main")
class TestSome : TestBase() {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    @Test
    fun Test_Duration() {
        println(Boolean::class.java.kotlinTypeName)

        var i = 0;
        thread(start = true, isDaemon = false) {
            i++;
        }

        Thread.sleep(300);

        println(i)
    }

}