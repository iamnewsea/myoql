package nbcp.web.base.mvc.handler

import nbcp.base.comm.JsonResult
import nbcp.base.extend.AsInt
import nbcp.base.extend.AsIntWithNull
import nbcp.base.extend.HasValue
import nbcp.base.utils.SpringUtil
import nbcp.mvc.comm.AdminSysOpsAction
import nbcp.myoql.bean.MongoFlywayBeanProcessor
import nbcp.myoql.db.db
import nbcp.myoql.db.mongo.delete
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
@ConditionalOnClass(value = arrayOf(MongoTemplate::class,  db::class))
open class BaseMongoFlywayServlet {
    /**
     * 清除 sysFlywayVersion.version 中 非0 的记录，并重新执行！
     */
    @RequestMapping("/ops/flyway/mongo/replay", method = arrayOf(RequestMethod.GET, RequestMethod.POST))
    fun doGet(version: String, request: HttpServletRequest, response: HttpServletResponse): JsonResult {
        db.morBase.sysFlywayVersion.delete()
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

