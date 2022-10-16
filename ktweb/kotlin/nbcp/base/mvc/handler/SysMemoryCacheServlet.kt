package nbcp.base.mvc.handler

import nbcp.comm.*
import nbcp.db.memoryCacheDb
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController


/**
 * Created by udi on 20-8-27.
 */
@AdminSysOpsAction
@RestController
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
open class SysMemoryCacheServlet {
    /**
     * @param key 用 AntPathMatcher 匹配,用 点 分隔每个部分
     */
    @RequestMapping("/sys/memory-cache/broke", method = arrayOf(RequestMethod.GET, RequestMethod.POST))
    fun doGet(@Require key: String): ListResult<String> {

        memoryCacheDb.brokeMemoryMatchCache(key)
            .apply {
                return ListResult.of(this)
            }
    }
}

