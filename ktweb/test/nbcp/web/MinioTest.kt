package nbcp.web

import nbcp.web.TestBase
import nbcp.web.base.mvc.service.upload.MinioBaseService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class MinioTest : TestBase() {
    @Autowired
    lateinit var minioClient: MinioBaseService

    @Test
    fun test() {
        var ret = minioClient.delete("http://192.168.5.219:9000/admin/2021-08/5fmzcd8aaups.jpg");
        println(ret.msg)
    }
}