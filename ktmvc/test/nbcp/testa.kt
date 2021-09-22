package nbcp

import nbcp.comm.LogLevelScope
import nbcp.comm.MyLogLevel
import nbcp.comm.usingScope
import nbcp.utils.SpringUtil
import org.junit.Test
import org.slf4j.LoggerFactory
import org.springframework.boot.logging.LogLevel
import org.springframework.stereotype.Component

@Component
class ff {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass);
    }

    @MyLogLevel(LogLevelScope.trace)
    fun abc(a: String) {
        logger.trace("OK:${a}")
    }
}

class testa : TestBase() {
    @Test
    fun test_log_level() {
        var d = SpringUtil.getBean<ff>().abc("OK")
        println(d)
    }
}