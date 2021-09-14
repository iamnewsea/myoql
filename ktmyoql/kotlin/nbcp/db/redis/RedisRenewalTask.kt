package nbcp.db.redis

import nbcp.comm.AsLong
import nbcp.db.cache.CacheForBrokeData
import nbcp.db.cache.CacheForSelectData
import nbcp.model.MasterAlternateMap
import nbcp.model.MasterAlternateSet
import nbcp.model.MasterAlternateStack
import nbcp.utils.SpringUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.core.StringRedisTemplate
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

@Configuration
@ConditionalOnProperty("spring.redis.host")
class RedisRenewalTask : InitializingBean {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)

        /**
         * 定时任务，使键续期。
         */
        fun setDelayRenewalKey(key: String, cacheSecond: Int) {
            renewal_cache.push(key, cacheSecond);
        }

        fun clearDelayRenewalKeys(vararg keys: String) {
            keys.forEach {
                renewal_cache.removeAll(it);
            }
        }


        /**
         * 缓存数据源，使用系统固定的数据库，不涉及分组及上下文切换。
         */
        private val redisTemplate by lazy {
            return@lazy SpringUtil.getBean<StringRedisTemplate>()
        }

        /**
         * 续期的 keys，value=过期时间，单位秒
         */
        private var renewal_cache = MasterAlternateMap<String, Int>({ a, b -> Math.max(a, b) }) { key, cacheSecond ->
            if (cacheSecond <= 0) {
                return@MasterAlternateMap
            }

            redisTemplate.expire(key, cacheSecond.AsLong(), TimeUnit.SECONDS)
        };

    }


    override fun afterPropertiesSet() {
        thread(start = true, isDaemon = true, name = "RedisRenewalTask") {
            while (true) {
                Thread.sleep(1000)
                try {
                    renewal_cache.consumeTask()
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    logger.error(ex.message);
                }
            }
        }
    }
}