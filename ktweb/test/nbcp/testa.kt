package nbcp


import nbcp.base.mvc.handler.DevDockerServlet
import nbcp.comm.ToJson
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