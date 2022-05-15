package nbcp.base.mvc.handler

import ch.qos.logback.classic.Level
import com.wf.captcha.ArithmeticCaptcha
import com.wf.captcha.utils.CaptchaUtil
import nbcp.comm.*
import nbcp.db.db
import nbcp.utils.CodeUtil
import nbcp.base.mvc.*
import nbcp.bean.FlywayBeanProcessor
import nbcp.db.mongo.delete
import nbcp.db.mongo.match_not_equal
import nbcp.utils.SpringUtil
import nbcp.web.tokenValue
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.lang.RuntimeException

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


/**
 * Created by udi on 20-8-27.
 */
@AdminSysOpsAction
@RestController
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(MongoTemplate::class)
@ConditionalOnProperty("spring.data.mongodb.uri")
open class BaseFlywayServlet {
    /**
     * 清除 sysFlywayVersion.version 中 非0 的记录，并重新执行！
     */
    @GetMapping("/ops/flyway/mongo/replay")
    fun doGet(version: String, request: HttpServletRequest, response: HttpServletResponse): JsonResult {
        db.mor_base.sysFlywayVersion.delete()
            .where { it.version match_not_equal 0 }
            .exec();

        val flyways = SpringUtil.getBeanWithNull(FlywayBeanProcessor::class.java)
        if (flyways == null) {
            return JsonResult.error("找不到Flyway相关配置！")
        }

        flyways.playFlyVersion(version.AsIntWithNull());

        return JsonResult()
    }
}

