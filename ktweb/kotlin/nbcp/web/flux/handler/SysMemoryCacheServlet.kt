package nbcp.web.flux.handler

import nbcp.base.comm.ListResult
import nbcp.base.db.memoryCacheDb
import nbcp.mvc.annotation.*
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
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

    /**
     * @param key 用 AntPathMatcher 匹配,用 点 分隔每个部分
     */
    @RequestMapping("/sys/memory-cache/broke", method = arrayOf(RequestMethod.GET, RequestMethod.POST))
    fun doGet(key: String): Mono<ListResult<String>> {
        memoryCacheDb.brokeMemoryMatchCache(key)
            .apply {
                return Mono.just(ListResult.of(this))
            }
    }
}
