package nbcp.base.flux.handler

import nbcp.comm.*
import nbcp.db.memoryCacheDb
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono


/**
 * Created by udi on 20-8-27.
 */
@AdminSysOpsAction
@RestController
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
open class SysMemoryCacheServlet {

    @RequestMapping("/sys/memory-cache/broke", method = arrayOf(RequestMethod.GET, RequestMethod.POST))
    fun doGet(key: String): Mono<ApiResult<Boolean>> {
        memoryCacheDb.brokeMemoryCache(key)
            .apply {
                return Mono.just(ApiResult.of(this))
            }
    }
}

