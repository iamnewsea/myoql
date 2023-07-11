package nbcp.myoql.bean


import nbcp.base.utils.SpringUtil
import nbcp.myoql.db.redis.MyRedisKeySerializerWithProductLine
import nbcp.myoql.db.redis.RedisRenewalDynamicService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.context.event.ApplicationStartedEvent
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
            if (bean.hashValueSerializer == null) {
                bean.hashValueSerializer = RedisSerializer.json()
            }

            if (bean.keySerializer == null) {
                bean.keySerializer = MyRedisKeySerializerWithProductLine();
            }

            if (SpringUtil.containsBean(RedisRenewalDynamicService::class.java) == false) {
                SpringUtil.registerBeanDefinition(RedisRenewalDynamicService())
            }
        }

        return ret;
    }

    private fun setStringRedisTemplate(bean: StringRedisTemplate) {

//        val type = bean::class.java;
//        val modifiersField = Field::class.java.getDeclaredField("modifiers");
//        modifiersField.isAccessible = true;
//
//
//        val valueOps = type.getDeclaredField("valueOps");
//        valueOps.isAccessible = true;
//        modifiersField.setInt(valueOps, valueOps.modifiers and Modifier.FINAL.inv());
    }


}