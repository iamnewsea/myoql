package nbcp.bean


import nbcp.db.redis.MyRedisKeySerializerWithProductLine
import nbcp.db.redis.RedisDataSource
import nbcp.db.redis.RedisRenewalDynamicService
import nbcp.utils.SpringUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.context.annotation.Import
import org.springframework.context.event.EventListener
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.serializer.RedisSerializer
import org.springframework.stereotype.Component

@Component

@ConditionalOnClass(StringRedisTemplate::class)
//@Import(value=[RedisDataSource::class])
class MyOqlRedisBeanConfig : BeanPostProcessor {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any? {
        var ret = super.postProcessAfterInitialization(bean, beanName)

        if (bean is StringRedisTemplate) {
            bean.hashValueSerializer = RedisSerializer.json()


            setStringRedisTemplate(bean);

            loadRedisDependencyBeans()
        }

        return ret;
    }

    private fun setStringRedisTemplate(bean: StringRedisTemplate) {
        bean.keySerializer = MyRedisKeySerializerWithProductLine();
//        val type = bean::class.java;
//        val modifiersField = Field::class.java.getDeclaredField("modifiers");
//        modifiersField.isAccessible = true;
//
//
//        val valueOps = type.getDeclaredField("valueOps");
//        valueOps.isAccessible = true;
//        modifiersField.setInt(valueOps, valueOps.modifiers and Modifier.FINAL.inv());
//
//
    }

    private fun loadRedisDependencyBeans() {
        if (SpringUtil.containsBean(RedisRenewalDynamicService::class.java) == false) {
            SpringUtil.registerBeanDefinition(RedisRenewalDynamicService())
        }
//        SpringUtil.registerBeanDefinition(RedisCacheAopService())
    }

    @EventListener
    fun app_started(event: ApplicationStartedEvent) {
//        //如果存在 Redis环境，但是没有 RedisCacheDbDynamicService，就构造一个，保证在Redis环境下至少一个。
//        if (SpringUtil.containsBean(StringRedisTemplate::class.java) &&
//            !SpringUtil.containsBean(RedisCacheDbDynamicService::class.java)
//        ) {
//            SpringUtil.registerBeanDefinition(RedisCacheDbDynamicService())
//        }
    }

}