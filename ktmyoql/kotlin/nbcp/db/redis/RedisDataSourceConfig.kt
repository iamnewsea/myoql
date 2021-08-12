package nbcp.db.redis

import nbcp.comm.HasValue
import nbcp.utils.SpringUtil
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
class RedisDataSourceConfig {

    @EventListener
    fun prepared(ev: ApplicationPreparedEvent) {
        if (SpringUtil.context.environment.getProperty("spring.redis.host").isNullOrEmpty() &&
            SpringUtil.context.environment.getProperty("spring.redis.url").isNullOrEmpty()
        ) {
            return;
        }
        if (SpringUtil.containsBean(RedisAutoConfiguration::class.java) == false) {
            return;
        }


        var factory = SpringUtil.binder.bindOrCreate("spring.redis", RedisProperties::class.java).getConnectionFactory()
        SpringUtil.registerBeanDefinition("redisFactory", factory)

        var stringRedisTemplate = factory.getStringRedisTemplate();
        SpringUtil.registerBeanDefinition("stringRedisTemplate", stringRedisTemplate)
    }


    private fun RedisProperties.getClusterConfiguration(): RedisClusterConfiguration? {

        val cluster = this.getCluster()
        if (cluster == null) return null;

        val config = RedisClusterConfiguration(cluster.nodes)
        if (cluster.maxRedirects != null) {
            config.setMaxRedirects(cluster.maxRedirects)
        }
        return config
    }

    private fun RedisProperties.Pool.getPoolConfig(): JedisPoolConfig {
        var config = JedisPoolConfig();
        config.maxIdle = this.maxIdle;
        config.minIdle = this.minIdle;
        config.maxTotal = this.maxActive;
        config.timeBetweenEvictionRunsMillis = this.timeBetweenEvictionRuns.toMillis();
        return config;
    }

    private fun RedisProperties.getStandardConfiguration(): RedisStandaloneConfiguration? {
        var config = RedisStandaloneConfiguration(this.host, this.port);

        if (this.password.HasValue) {
            config.password = RedisPassword.of(this.password)
        }
        return config;
    }

    private fun RedisProperties.getConnectionFactory(): JedisConnectionFactory {
        var config = this.getClusterConfiguration();

        var factory: JedisConnectionFactory?
        if (config != null) {
            factory = JedisConnectionFactory(config, this.jedis.pool.getPoolConfig())
        } else {
            var config2 = this.getStandardConfiguration();
            factory = JedisConnectionFactory(config2)
        }
        return factory;
    }

//    private fun JedisConnectionFactory.getAnyTypeRedisTemplate(): AnyTypeRedisTemplate{
//        var template = AnyTypeRedisTemplate()
//        template.connectionFactory = this;
//        template.keySerializer = RedisSerializer.string()
//        template.valueSerializer = RedisSerializer.string()
//        template.hashKeySerializer = RedisSerializer.string()
//        template.hashValueSerializer = RedisSerializer.json()
//        template.afterPropertiesSet();
//        return template;
//    }

    private fun JedisConnectionFactory.getStringRedisTemplate(): StringRedisTemplate {
        var template = StringRedisTemplate()
        template.connectionFactory = this;
        template.keySerializer = RedisSerializer.string()
        template.valueSerializer = RedisSerializer.string()
        template.hashKeySerializer = RedisSerializer.string()
        template.hashValueSerializer = RedisSerializer.json()
        template.afterPropertiesSet();
        return template;
    }
}