package nbcp.base.mvc.handler

import nbcp.comm.*
import nbcp.db.db
import nbcp.bean.MongoFlywayBeanProcessor
import nbcp.db.mongo.delete
import nbcp.db.mongo.match_gte
import nbcp.db.mongo.match_not_equal
import nbcp.utils.SpringUtil
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


/**
 * Created by udi on 20-8-27.
 */
@AdminSysOpsAction
@RestController
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(MongoTemplate::class)
open class BaseMongoFlywayServlet {
    /**
     * 清除 sysFlywayVersion.version 中 非0 的记录，并重新执行！
     */
    @RequestMapping("/ops/flyway/mongo/replay", method = arrayOf(RequestMethod.GET, RequestMethod.POST))
    fun doGet(version: String, request: HttpServletRequest, response: HttpServletResponse): JsonResult {
        db.mor_base.sysFlywayVersion.delete()
            .where { it.version match_not_equal 0 }
            .apply {
                if (version.HasValue) {
                    this.where { it.version match_gte version.AsInt() }
                }
            }
            .exec();

        val flyways = SpringUtil.getBeanWithNull(MongoFlywayBeanProcessor::class.java)
        if (flyways == null) {
            return JsonResult.error("找不到Flyway相关配置！")
        }

        flyways.playFlyVersion(version.AsIntWithNull());

        return JsonResult()
    }
}

