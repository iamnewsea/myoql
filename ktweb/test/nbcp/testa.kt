package nbcp


import ch.qos.logback.classic.Level
import nbcp.base.mvc.handler.DevDockerServlet
import nbcp.base.mvc.handler.HiServlet
import nbcp.comm.ToJson
import nbcp.comm.usingScope
import nbcp.tool.UserCodeGenerator
import nbcp.utils.ProgramCoderUtil
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired

class testa : TestBase() {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass);
    }

    @Autowired
    lateinit var devDockerServlet: DevDockerServlet;

    @Test
    fun abc(){
        var d = devDockerServlet.getContainers("");
        println(d.ToJson())
    }
}