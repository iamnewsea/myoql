package nbcp


import ch.qos.logback.classic.Level
import nbcp.comm.LogScope
import nbcp.comm.using
import nbcp.tool.CodeGenerator
import org.junit.Test
import org.slf4j.LoggerFactory

class testa : TestBase() {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass);
    }

    @Test
    fun abc() {
        var txt = CodeGenerator.genMvc()
        println(txt)
    }
}