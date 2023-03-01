package nbcp.web.flux.handler

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import nbcp.base.comm.ListResult
import nbcp.base.comm.config
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
        var list = config.cacheContainers.brokeWithMatch(key);

        return Mono.just(ListResult.of(list))
    }
}

