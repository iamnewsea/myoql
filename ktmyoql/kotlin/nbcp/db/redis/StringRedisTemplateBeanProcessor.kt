package nbcp.db.redis

import nbcp.db.cache.RedisCacheDbDynamicService
import nbcp.utils.SpringUtil
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.context.annotation.Import
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.serializer.RedisSerializer
import org.springframework.stereotype.Component


@Component
@Import(SpringUtil::class)
class StringRedisTemplateBeanProcessor : BeanPostProcessor {
    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any? {
        if (bean.javaClass == StringRedisTemplate::class.java) {
            var stringRedisTemplate = bean as StringRedisTemplate
            stringRedisTemplate.hashValueSerializer = RedisSerializer.json()


            SpringUtil.registerBeanDefinition("redisCacheDbDynamicService", RedisCacheDbDynamicService())
            SpringUtil.registerBeanDefinition("redisRenewalDynamicService", RedisRenewalDynamicService())
        }
        return super.postProcessAfterInitialization(bean, beanName)
    }
}