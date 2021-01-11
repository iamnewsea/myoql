package nbcp.utils

import nbcp.TestBase
import org.junit.Test


class HttpUtilTest : TestBase() {
    @Test
    fun abfc() {
        var http = HttpUtil("http://localhost:8002/sys/upload")
        http.request.headers.set("token", "sf5ac1c8piscg0")
        http.uploadFile("""F:\BaiduNetdiskDownload\阿里微服务架构Spring Cloud Alibaba实战-1015.mp4""")
        println("OK")
    }
}