package nbcp


import ch.qos.logback.classic.Level
import nbcp.base.extend.LogScope
import nbcp.base.extend.using
import org.junit.Test
import org.slf4j.LoggerFactory

class testa : TestBase() {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass);
    }

    @Test
    fun abc() {
        using(LogScope(Level.DEBUG_INT)) {
            logger.info("iii")
        }
        println("OK")
    }
}