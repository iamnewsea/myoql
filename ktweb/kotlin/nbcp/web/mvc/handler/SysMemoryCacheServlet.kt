package nbcp.web.mvc.handler

import nbcp.base.comm.ListResult
import nbcp.base.db.memoryCacheDb
import nbcp.mvc.comm.AdminSysOpsAction
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
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
    fun doGet(@nbcp.base.comm.Require key: String): ListResult<String> {

        memoryCacheDb.brokeMemoryMatchCache(key)
            .apply {
                return ListResult.of(this)
            }
    }
}

