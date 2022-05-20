package nbcp


import ch.qos.logback.classic.Level
import nbcp.base.mvc.handler.HiServlet
import nbcp.comm.usingScope
import nbcp.tool.UserCodeGenerator
import nbcp.utils.ProgramCoderUtil
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

class testa : TestBase() {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass);
    }


    @Test
    fun abc(){
        var pu =  ProgramCoderUtil()
        var d = pu.getFeignClientCode(HiServlet::class.java , "hi")
        println(d)
    }
}