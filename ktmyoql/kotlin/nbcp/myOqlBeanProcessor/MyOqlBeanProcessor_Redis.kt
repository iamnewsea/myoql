package nbcp.myOqlBeanProcessor


import nbcp.db.cache.RedisCacheDbDynamicService
import nbcp.db.redis.RedisRenewalDynamicService
import nbcp.utils.SpringUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Import
import org.springframework.context.event.EventListener
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.serializer.RedisSerializer
import org.springframework.stereotype.Component

@Component
@Import(SpringUtil::class)
@ConditionalOnClass(StringRedisTemplate::class)
class MyOqlBeanProcessor_Redis : BeanPostProcessor {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any? {
        var ret = super.postProcessAfterInitialization(bean, beanName)

        if (bean is StringRedisTemplate) {
            bean.hashValueSerializer = RedisSerializer.json()

            loadRedisDependencyBeans()
        }

        return ret;
    }

    private fun loadRedisDependencyBeans() {
        SpringUtil.registerBeanDefinition(RedisRenewalDynamicService())
    }

    @EventListener
    fun onApplicationReady(event: ApplicationReadyEvent) {
        //如果存在 Redis环境，但是没有 RedisCacheDbDynamicService，就构造一个，保证在Redis环境下至少一个。
        if (SpringUtil.containsBean(StringRedisTemplate::class.java) &&
            !SpringUtil.containsBean(RedisCacheDbDynamicService::class.java)
        ) {
            SpringUtil.registerBeanDefinition(RedisCacheDbDynamicService())
        }
    }

}