package nbcp


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
        using(LogScope.LogDebugLevel) {
            logger.info("iii")
        }
        println("OK")
    }
}