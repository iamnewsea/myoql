package nbcp.db.redis

import nbcp.comm.HasValue
import nbcp.utils.SpringUtil
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration
import org.springframework.boot.autoconfigure.data.redis.RedisProperties
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.context.event.ApplicationPreparedEvent
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.context.annotation.Import
import org.springframework.context.event.EventListener
import org.springframework.data.redis.connection.RedisClusterConfiguration
import org.springframework.data.redis.connection.RedisConfiguration
import org.springframework.data.redis.connection.RedisPassword
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.serializer.RedisSerializer
import org.springframework.stereotype.Component
import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPoolConfig

@Component
@Import(SpringUtil::class)
class StringRedisTemplateBeanProcessor : BeanPostProcessor {
    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any? {
        if (bean.javaClass == StringRedisTemplate::class.java) {
            var stringRedisTemplate = bean as StringRedisTemplate
            stringRedisTemplate.hashValueSerializer = RedisSerializer.json()
        }
        return super.postProcessAfterInitialization(bean, beanName)
    }
}