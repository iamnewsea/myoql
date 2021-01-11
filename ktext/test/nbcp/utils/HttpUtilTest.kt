package nbcp.utils

import nbcp.TestBase
import org.junit.Test


class HttpUtilTest : TestBase() {
    @Test
    fun abfc() {
        var http = HttpUtil("http://localhost:8002/sys/upload")
        http.setRequest { it.headers.set("token", "sf5ac1c8piscg0") }
        http.uploadFile("""e:\软件\cn_office_professional_plus_2013_x64_dvd_1134006.iso""")
        println("OK")
    }
}