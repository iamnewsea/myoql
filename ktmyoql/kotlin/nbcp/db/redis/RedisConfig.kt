package nbcp.db.redis

/**
 * Created by udi on 17-3-19.
 */
import com.fasterxml.jackson.databind.ObjectMapper
import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.sync.RedisCommands
import io.lettuce.core.support.ConnectionPoolSupport
import nbcp.base.extend.FieldTypeJsonMapper
import nbcp.base.extend.HasValue
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import org.springframework.data.redis.connection.DefaultStringRedisConnection
import org.springframework.data.redis.connection.RedisConnection
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.SetOperations
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer
import org.springframework.stereotype.Component
import java.time.Duration

//
////@Suppress("SpringKotlinAutowiring")
//@Component
//@Lazy
////@EnableCaching
//open class RedisConfig {
//    //: CachingConfigurerSupport() {
//    companion object {
//        internal val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
//        protected var pool_string  = linkedMapOf<String, GenericObjectPool<StatefulRedisConnection<String, String>>>()
//        protected var pool_byteArray = linkedMapOf<String, GenericObjectPool<StatefulRedisConnection<String, ByteArray>>>()
//    }
//
//    @Value("\${spring.redis.host:}")
//    private var host: String = ""
//
//    @Value("\${spring.redis.port:0}")
//    private var port: Int = 0
//
//    @Value("\${spring.redis.timeout:PT3S}")
//    private var timeout: String = ""
//
//    @Value("\${spring.redis.lettuce.pool.max-active:8}")
//    private var maxActive: Int = 0
//
//    @Value("\${spring.redis.lettuce.pool.max-idle:0}")
//    private var maxIdle: Int = 0
//
//    @Value("\${spring.redis.lettuce.pool.max-wait:}")
//    private var maxWaitMillis: String = ""
//
//    @Value("\${spring.redis.database:0}")
//    private var database: Int = 0
//
//    @Value("\${spring.redis.password:}")
//    private var password: String = ""
//
//    @Value("\${shop.redis.offset:false}")
//    private var offset: Boolean = false
//
//    private fun getRedisURI(dbOffset: Int): RedisURI {
//        var redisBuilder = RedisURI.builder();
//        if(host.isEmpty()){
//            return redisBuilder.build();
//        }
//
//        redisBuilder.withHost(host);
//
//        if (port != 0) {
//            redisBuilder.withPort(port);
//        }
//
//        redisBuilder.withTimeout(Duration.parse(timeout))
//        redisBuilder.withDatabase(database + (if (offset) dbOffset else 0));
//        if (password.HasValue) {
//            redisBuilder.withPassword(password)
//        }
//
//        return redisBuilder.build();
//    }
//
//    fun <R> stringCommand(dbOffset: Int, block: (RedisCommands<String, String>) -> R): R {
//        var pool = pool_string.getOrPut(dbOffset.toString() + ":String") {
//            var url = getRedisURI(dbOffset)
//
//            var client = RedisClient.create(url)
//            val pool = ConnectionPoolSupport.createGenericObjectPool({ client.connect() }, GenericObjectPoolConfig<StatefulRedisConnection<String,String>>())
//
//            if (maxIdle > 0) {
//                pool.maxIdle = maxIdle
//            }
//
//            if (maxActive > 0) {
//                pool.maxTotal = maxActive
//            }
//
//            if (maxWaitMillis.HasValue) {
//                pool.maxWaitMillis = Duration.parse(maxWaitMillis).toMillis();
//            }
//
//            return@getOrPut pool
//        }
//
//        var connection = pool.borrowObject();
//        var command = connection.sync()
//
//        try {
//            return block(command!!)
//        } catch (e: Exception) {
//            throw e;
//        } finally {
//            connection.close();
//        }
//    }
//
//    fun <R> byteArrayCommand(dbOffset: Int, block: (RedisCommands<String, ByteArray>) -> R): R {
//        var pool = pool_byteArray.getOrPut(dbOffset.toString() + ":Byte") {
//            var url = getRedisURI(dbOffset)
//
//            var client = RedisClient.create(url)
//            val pool = ConnectionPoolSupport.createGenericObjectPool({ client.connect(RedisStringBlobCodec.INSTANCE) }, GenericObjectPoolConfig<StatefulRedisConnection<String, ByteArray>>())
//
//            if (maxIdle > 0) {
//                pool.maxIdle = maxIdle
//            }
//
//            pool.maxTotal = maxActive
//
//
//            if (maxWaitMillis.HasValue) {
//                pool.maxWaitMillis = Duration.parse(maxWaitMillis).toMillis();
//            }
//
//            return@getOrPut pool
//        }
//
//        var connection = pool.borrowObject();
//        var command = connection.sync()
//
//        try {
//            return block(command!!)
//        } finally {
//            connection.close();
//        }
//    }
//}

class AnyTypeRedisTemplate() : RedisTemplate<String, Any>() {
    init {
        //使用Jackson2JsonRedisSerializer来序列化和反序列化redis的value值
//        var serializer = Jackson2JsonRedisSerializer(Any::class.java);
//        serializer.setObjectMapper(FieldTypeJsonMapper.instance);

        this.keySerializer = RedisSerializer.string()
        this.valueSerializer = RedisSerializer.json()
        this.hashKeySerializer = RedisSerializer.string()
        this.hashValueSerializer = RedisSerializer.json()
    }

    constructor(connectionFactory: RedisConnectionFactory) : this() {
        this.connectionFactory = connectionFactory
        this.afterPropertiesSet()
    }

    override fun preProcessConnection(connection: RedisConnection, existingConnection: Boolean): RedisConnection {
        return DefaultStringRedisConnection(connection)
    }

    override fun opsForSet(): SetOperations<String, Any> {
        return super.opsForSet()
    }
}


@Configuration
@AutoConfigureAfter(RedisAutoConfiguration::class)
//@ConditionalOnProperty("spring.redis.host")
@ConditionalOnBean(RedisConnectionFactory::class)
class RedisConfig {
    @Bean
    fun redisKeyCommand(connectionFactory: RedisConnectionFactory): AnyTypeRedisTemplate {
        var template  = AnyTypeRedisTemplate()
        template.connectionFactory = connectionFactory;
        template.afterPropertiesSet();
        return template;
    }
}

//
//@Configuration
//@AutoConfigureAfter(RedisAutoConfiguration::class)
//class RedisConfig {
//
//    /**
//     * 集群不能使用 除0号数据库之外的数据库， 所以不存在切换数据的情况。
//     * @return
//     */
//    @Bean
//    fun redisTemplate(redisConnectionFactory: RedisConnectionFactory): RedisTemplate<String, Any> {
//
//        var template = RedisTemplate<String, Any>();
//        template.setConnectionFactory(redisConnectionFactory);
//
//        var serializer = Jackson2JsonRedisSerializer(Any::class.java);
//        serializer.setObjectMapper(FieldTypeJsonMapper.instance);
//
//        template.setValueSerializer(serializer);
//        //使用StringRedisSerializer来序列化和反序列化redis的key值
//        template.setKeySerializer(StringRedisSerializer());
//        template.setHashKeySerializer(StringRedisSerializer());
//        template.setHashValueSerializer(serializer);
//        template.afterPropertiesSet();
//
//        return template;
//    }
//}

