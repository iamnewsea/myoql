package nbcp.utils

import nbcp.TestBase
import nbcp.comm.StringMap
import nbcp.comm.formatWithJson
import nbcp.comm.usingScope
import org.junit.Test
import org.springframework.boot.logging.LogLevel
import java.io.FileInputStream


class HttpUtilTest : TestBase() {
    @Test
    fun abfc() {
        var http = HttpUtil("http://localhost:8202/sys/upload")
        http.request.headers.set("group", "lowcode")
        var ret = http.uploadFile("DevOps流程.vsdx", FileInputStream("""D:\能科资料\DevOps流程.vsdx"""))
        println(ret)
    }

    @Test
    fun abfc2() {
        usingScope(LogLevel.INFO) {
            var http = HttpUtil("http://saas-demo.nancal.com:7003/c/login")
            http.request.headers["Content-Type"] = "application/x-www-form-urlencoded"

            println(http.request.contentType)
            var authInfoMap = StringMap(
                "loginName" to "admin",
                "password" to "Nancal1234"
            )

            var msg = http.doPost("principal=@loginName&password=@password".formatWithJson(authInfoMap, "@"))
        }
    }
}