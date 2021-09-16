package nbcp.db.cache

import nbcp.comm.AsLong
import nbcp.comm.FromJson
import nbcp.comm.config
import nbcp.comm.const
import nbcp.db.db
import nbcp.db.redis.scanKeys
import nbcp.utils.CodeUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.connection.Message
import org.springframework.data.redis.connection.MessageListener
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.data.redis.listener.PatternTopic
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter
import java.time.LocalDateTime
import kotlin.concurrent.thread

/**
 * 各系统继承该类，进行消费
 */
open class RedisCacheDbDynamicService : InitializingBean {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)

        val CHANNELNAME = "sc-borker-version"
    }


    override fun afterPropertiesSet() {

//        var container = RedisMessageListenerContainer()
//        var listener = MessageListenerAdapter(SqlBrokerListener(this), "handleMessage");
//
//        container.connectionFactory = redisTemplate.connectionFactory;
//
//        container.addMessageListener(
//            listener,
//            ChannelTopic("${CHANNELNAME}-${config.applicationName}")
//        )

        startSubscribe();
    }

    fun startSubscribe() {
        thread(start = true, isDaemon = true, name = "RedisCacheDbDynamicService") {
            while (true) {
                try {
                    redisTemplate.connectionFactory.connection.subscribe(
                        CacheForBrokeMessageListener({
                            CacheForBrokeDataWorkService.brokeCacheItem(redisTemplate, it);
                        }),
                        "${CHANNELNAME}-${config.applicationName}".toByteArray(const.utf8)
                    )
                } catch (ex: Exception) {
                    //可能断网
                    logger.error(ex.message);
                    Thread.sleep(1000);
                }
            }
        }
    }

    /**
     * 发布队列的通知。
     */
    fun publish() {
        redisTemplate.convertAndSend(
            "${CHANNELNAME}-${config.applicationName}", CodeUtil.getCode()
        )
    }

    /**
     * 缓存数据源，使用系统固定的数据库，不涉及分组及上下文切换。
     */
    @Autowired
    lateinit var redisTemplate: StringRedisTemplate;


}

