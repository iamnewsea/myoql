package nbcp

import nbcp.base.mvc.service.upload.MinioBaseService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.util.AntPathMatcher

class AntMatcherTest : TestBase() {


    @Test
    fun test() {
        var matcher = AntPathMatcher()
        println(matcher.match("/a/{type}/c", "/a/b/c"))
        println(matcher.matchStart("/a/{type}/c", "/a/b/c/d"))
    }
}