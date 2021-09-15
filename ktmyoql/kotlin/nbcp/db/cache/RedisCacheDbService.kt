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
import org.springframework.data.redis.listener.PatternTopic
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter
import java.time.LocalDateTime

/**
 *
 */
class RedisCacheDbDynamicService : InitializingBean {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
        private var working = false;
        var lastConsumerAt = LocalDateTime.now();
    }

    override fun afterPropertiesSet() {
        var container = RedisMessageListenerContainer()
        container.connectionFactory = redisTemplate.connectionFactory;
        container.addMessageListener(
            SqlBrokerListener(this),
            PatternTopic("sc-borker-version-${config.applicationName}")
        )
    }

    /**
     * 发布队列的通知。
     */
    fun publish() {
        redisTemplate.convertAndSend(
            "sc-borker-version-${config.applicationName}", CodeUtil.getCode().toByteArray()
        )
    }


    class SqlBrokerListener(var p: RedisCacheDbDynamicService) : MessageListener {
        companion object {
        }

        /**
         * 驱动器，驱动从 redis 队列中取出，进行消费。
         */
        override fun onMessage(p0: Message, p1: ByteArray?) {
            if (working) return;
            working = true;

            while (true) {
                var cacheBrokeStringValue = db.rer_base.sqlCacheBroker.pop(config.applicationName);
                if (cacheBrokeStringValue.isEmpty()) {
                    break;
                }

                var cacheBroke = cacheBrokeStringValue.FromJson<CacheForBrokeData>()!!
                p.brokeCacheItem(cacheBroke);
            }

            lastConsumerAt = LocalDateTime.now();
            working = false;
        }
    }


    /**
     * 缓存数据源，使用系统固定的数据库，不涉及分组及上下文切换。
     */
    @Autowired
    lateinit var redisTemplate: StringRedisTemplate;

    fun brokeCacheItem(cacheBroke: CacheForBrokeData) {
        var pattern = cacheBroke.getTablePattern()

        for (i in 0..999) {
            var all_keys = redisTemplate.scanKeys(pattern);
            if (all_keys.any() == false) {
                break;
            }


            //如果是删除全表。
            if (cacheBroke.key.isEmpty() || cacheBroke.value.isEmpty()) {
                redisTemplate.delete(all_keys);
                continue;
            }

            //先移除不含 ~ 的key
            var like_sql_keys = all_keys.filter { it.contains(CacheForSelectData.KEY_VALUE_JOIN_CHAR) == false };
            if (like_sql_keys.any()) {
                redisTemplate.delete(like_sql_keys)
            }

            var other_group_keys_pattern =
                "${CacheForSelectData.GROUP_JOIN_CHAR}${cacheBroke.key}${CacheForSelectData.KEY_VALUE_JOIN_CHAR}"
            //破坏其它维度的分组
            var other_group_keys =
                all_keys.filter { it.contains(other_group_keys_pattern) == false };
            if (other_group_keys.any()) {
                redisTemplate.delete(other_group_keys);
            }

            var this_group_keys_pattern =
                "${CacheForSelectData.GROUP_JOIN_CHAR}${cacheBroke.key}${CacheForSelectData.KEY_VALUE_JOIN_CHAR}${cacheBroke.value}${CacheForSelectData.GROUP_JOIN_CHAR}"
            //再精准破坏 key,value分组的。
            var this_group_keys =
                all_keys.filter { it.contains(this_group_keys_pattern) };

            if (this_group_keys.any()) {
                redisTemplate.delete(this_group_keys);
            }
        }


        pattern = cacheBroke.getJoinTablePattern();

        for (i in 0..999) {
            var list = redisTemplate.scanKeys(pattern);
            if (list.any() == false) {
                break;
            }

            redisTemplate.delete(list);
        }
    }

}

