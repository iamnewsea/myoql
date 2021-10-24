package nbcp.myOqlBeanProcessor

import nbcp.db.mybatis.MyBatisPlusRedisCacheConfig
import nbcp.db.mysql.service.UploadFileSqlService
import nbcp.utils.SpringUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.context.annotation.Import
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component

@Component

@ConditionalOnClass(NamedParameterJdbcTemplate::class)
class MyOqlJdbcTemplateConfig : BeanPostProcessor {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any? {
        var ret = super.postProcessAfterInitialization(bean, beanName)

        if (bean is NamedParameterJdbcTemplate) {
            loadJdbcDependencyBeans()
        }

        return ret
    }


    private fun loadJdbcDependencyBeans() {
        SpringUtil.registerBeanDefinition(UploadFileSqlService())
        SpringUtil.registerBeanDefinition(MyBatisPlusRedisCacheConfig())
    }

}