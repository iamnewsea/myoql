package nbcp

import nbcp.base.TestBase
import nbcp.base.enums.*
import nbcp.base.utils.*
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class ff {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass);
    }

    @nbcp.base.comm.MyLogLevel(LogLevelScopeEnum.trace)
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