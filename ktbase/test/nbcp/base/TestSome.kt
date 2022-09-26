package nbcp.base

import nbcp.TestBase
import nbcp.comm.GroupLog
import nbcp.comm.*
import org.junit.jupiter.api.Test
import org.slf4j.Logger
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
        logger.error("III")
        logger.abcv("OK")
    }

}

inline fun Logger.abcv(msg: String) {
    this.error(msg)
}