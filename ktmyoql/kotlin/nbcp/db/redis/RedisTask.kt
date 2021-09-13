package nbcp.db.redis

import nbcp.comm.AsLong
import nbcp.comm.HasValue
import nbcp.db.cache.CacheForBroke
import nbcp.db.db
import nbcp.model.MasterAlternateStack
import nbcp.utils.SpringUtil
import org.springframework.beans.factory.InitializingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

@Configuration
@ConditionalOnProperty("spring.redis.host")
class RedisTask : InitializingBean {
    companion object {
        /**
         * 定时任务，使键续期。
         */
        fun setDelayRenewalKey(key: String, cacheSecond: Int) {
            renewal_cache.push(key to cacheSecond);
        }

        fun clearDelayRenewalKeys(vararg keys: String) {
            keys.forEach {
                renewal_cache.removeAll(it);
            }
        }


        fun setDelayBrokeCacheKey(key: CacheForBroke) {
            broke_cache.push(key)
        }


        private val redisTemplate: StringRedisTemplate
            get() {
                return db.redis.getStringRedisTemplate("")
            }

        /**
         * 续期的 keys，value=过期时间，单位秒
         */
        private var renewal_cache = MasterAlternateStack<Pair<String, Int>>() {
            var key = it.first;
            var cacheSecond = it.second;

            if (cacheSecond <= 0) {
                return@MasterAlternateStack
            }

            redisTemplate.expire(key, cacheSecond.AsLong(), TimeUnit.SECONDS)
        };
        private var broke_cache = MasterAlternateStack<CacheForBroke>() {
            var pattern = "";
            if (it.key.HasValue && it.value.HasValue) {
                pattern = "sc:${it.table}:*(${it.key}-${it.value})*";
            } else {
                pattern = "sc:${it.table}:*";
            }

            for (i in 0..99) {
                var list = redisTemplate.scanKeys(pattern);
                if (list.any() == false) {
                    break;
                }

                redisTemplate.delete(list);
            }

            pattern = "sc:*[${it.table}]*"

            for (i in 0..99) {
                var list = redisTemplate.scanKeys(pattern);
                if (list.any() == false) {
                    break;
                }

                redisTemplate.delete(list);
            }
        }
    }


    override fun afterPropertiesSet() {
        thread(start = true, isDaemon = true, name = "MyOqlRedisTask") {
            while (true) {
                Thread.sleep(1000)
                renewal_cache.consumeTask()
                broke_cache.consumeTask()
            }
        }
    }
}