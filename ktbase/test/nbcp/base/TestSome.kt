package nbcp.base

import nbcp.base.annotation.*
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory

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