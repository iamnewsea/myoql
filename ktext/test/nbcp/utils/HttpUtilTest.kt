package nbcp.utils

import nbcp.TestBase
import nbcp.comm.LogScope
import nbcp.comm.StringMap
import nbcp.comm.formatWithJson
import nbcp.comm.usingScope
import org.junit.Test


class HttpUtilTest : TestBase() {
    @Test
    fun abfc() {
        var http = HttpUtil("http://localhost:8002/sys/upload")
        http.request.headers.set("token", "sf5ac1c8piscg0")
        http.uploadFile("""F:\BaiduNetdiskDownload\阿里微服务架构Spring Cloud Alibaba实战-1015.mp4""")
        println("OK")
    }

    @Test
    fun abfc2() {
        usingScope(LogScope.info) {
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