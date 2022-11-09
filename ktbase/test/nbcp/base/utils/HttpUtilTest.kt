package nbcp.base.utils

import nbcp.base.TestBase
import nbcp.base.comm.StringMap
import nbcp.base.enums.LogLevelScopeEnum
import nbcp.base.extend.formatWithJson
import nbcp.base.extend.usingScope
import org.junit.jupiter.api.Test
import java.io.File


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
        usingScope(LogLevelScopeEnum.info) {
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