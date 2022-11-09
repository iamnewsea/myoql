package nbcp


import nbcp.base.TestBase
import nbcp.base.comm.*;
import nbcp.base.db.*;
import nbcp.base.enums.*;
import nbcp.base.extend.*;
import nbcp.base.utils.*;
import nbcp.web.base.mvc.handler.DevDockerServlet
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