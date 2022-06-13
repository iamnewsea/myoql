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
                "cmd.zip" to File("""/home/udi/Downloads/jenkins-ops-cmd.zip""")
            )
        )
        println(ret)
    }

    @Test
    fun abfc2() {
        usingScope(LogLevelScope.info) {
            var http = HttpUtil("http://saas-demo.nancal.com:7003/c/login")
            http.request.headers["Content-Type"] = "application/x-www-form-urlencoded"

            println(http.request.contentType)
            var authInfoMap = StringMap(
                "loginName" to "admin",
                "password" to "Nancal1234"
            )

            http.doPost("principal=@loginName&password=@password".formatWithJson(authInfoMap, "@"))
        }
    }
}