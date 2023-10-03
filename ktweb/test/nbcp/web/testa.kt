package nbcp.web


import nbcp.base.extend.ToJson
import nbcp.web.sys.handler.DevDockerServlet
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