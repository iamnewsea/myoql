package nbcp.base.config


import nbcp.base.extend.Important
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.stereotype.Component

/**
 * 给 scheduler 加开关
 */
@EnableAsync
@Component
@EnableScheduling
@ConditionalOnProperty(name = ["app.scheduler"], havingValue = "true", matchIfMissing = false)
class TaskConfig : InitializingBean {
    companion object{
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    override fun afterPropertiesSet() {
        logger.Important("~~-~~-~~-~~  @EnableScheduling ~~-~~-~~-~~ ")
    }
}