package nbcp.mvc.dev2

import io.swagger.annotations.Api
import nbapp.mvc.dev2.AppCacheTestKotlinService
import org.springframework.web.bind.annotation.RestController
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.annotation.RequestMapping

/**
 * Created by CodeGenerator at 2021-04-11 23:42:19
 */
@Api(description = "数据连接", tags = arrayOf("DbConnection"))
@RestController
@RequestMapping("/test/kt")
class CacheTestKotlinController {

    @Autowired
    lateinit var appCacheTestKotlinService: AppCacheTestKotlinService;

    @GetMapping("/cache")
    fun cach_city1() {
        println("查，city:1")
        appCacheTestKotlinService.code_cache_select(1)
        println("查，city:2")
        appCacheTestKotlinService.code_cache_select(2)
        println("查，city:1，从缓存中出")
        appCacheTestKotlinService.code_cache_select(1)
        println("破 city:1")
        appCacheTestKotlinService.code_cache_broke(1)
        println("查，city:1，应该没有")
        appCacheTestKotlinService.code_cache_select(1)
        println("查，city:2，从缓存中出")
        appCacheTestKotlinService.code_cache_select(2)
    }
}


