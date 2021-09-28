package nbcp


import ch.qos.logback.classic.Level
import nbcp.comm.usingScope
import nbcp.tool.UserCodeGenerator
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

class testa : TestBase() {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass);
    }
}