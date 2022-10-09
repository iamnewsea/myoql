package nbcp.utils

import nbcp.TestBase
import nbcp.comm.LogLevelScope
import nbcp.comm.StringMap
import nbcp.comm.formatWithJson
import nbcp.comm.usingScope
import org.junit.jupiter.api.Test
import org.springframework.boot.logging.LogLevel
import java.io.File
import java.io.FileInputStream


class HttpUtilTest : TestBase() {
    @Test
    fun abfc() {
        var http = HttpUtil("http://localhost:8089/sys/upload")
        http.request.headers.set("group", "lowcode")
        var ret = http.submitForm(
            mapOf(
                "a" to "b",
                "tar.txt" to File("""/opt/idea-2022.1/Install-Linux-tar.txt""")
            )
        )
        println(ret)
    }

    @Test
    fun abfc2() {
        usingScope(LogLevelScope.info) {
            var http = HttpUtil("http://saas-demo.nancal3.com:8003/c/login").withMaxTryTimes(3)
            http.request.headers["Content-Type"] = "application/x-www-form-urlencoded"
            http.withMaxTryTimes(3)
            println(http.request.contentType)
            var authInfoMap = StringMap(
                "loginName" to "admin",
                "password" to "Nancal1234"
            )

            http.doPost("principal=@loginName&password=@password".formatWithJson(authInfoMap, "@"))
        }


        println("done")
    }
}