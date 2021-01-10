package nbcp.utils

import nbcp.TestBase
import org.junit.Test


class HttpUtilTest : TestBase() {
    @Test
    fun abfc() {
        var http = HttpUtil("http://localhost:8002/hi")
        http.setRequest { it.setRequestProperty("token", "abc") }
        http.uploadFile("""d:\soft\CentOS-7-x86_64-Minimal-1810.iso""")
        println("OK")
    }
}