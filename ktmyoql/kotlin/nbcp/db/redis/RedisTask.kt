package nbcp.db.redis

import nbcp.comm.AsLong
import nbcp.comm.HasValue
import nbcp.db.cache.CacheForBroke
import nbcp.db.cache.CacheForBrokeData
import nbcp.db.db
import nbcp.model.MasterAlternateStack
import nbcp.utils.SpringUtil
import org.slf4j.LoggerFactory
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
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)

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


        fun setDelayBrokeCacheKey(key: CacheForBrokeData) {
            broke_cache.push(key)
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
        private var renewal_cache = MasterAlternateStack<Pair<String, Int>>() {
            var key = it.first;
            var cacheSecond = it.second;

            if (cacheSecond <= 0) {
                return@MasterAlternateStack
            }

            redisTemplate.expire(key, cacheSecond.AsLong(), TimeUnit.SECONDS)
        };

        /**
         * 缓存破坏
         */
        private var broke_cache = MasterAlternateStack<CacheForBrokeData>() { cacheBroke ->
            var pattern = "sc:${cacheBroke.table}:*";

            for (i in 0..999) {
                var list = redisTemplate.scanKeys(pattern);
                if (list.any() == false) {
                    break;
                }


                //如果是删除全表。
                if (cacheBroke.key.isEmpty() || cacheBroke.value.isEmpty()) {
                    redisTemplate.delete(list);
                    continue;
                }

                //先移除不含 @ 的key
                var list_like_sql_keys = list.filter { it.contains("@") == false };
                if (list_like_sql_keys.any()) {
                    redisTemplate.delete(list_like_sql_keys)
                }

                //再精准破坏 key,value分组的。
                var list_keys = list.filter { it.contains(":${cacheBroke.key}@${cacheBroke.value}:") };

                if (list_keys.any()) {
                    redisTemplate.delete(list_keys);
                }
            }


            pattern = "sc:*|${cacheBroke.table}|*"

            for (i in 0..999) {
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
                try {
                    renewal_cache.consumeTask()
                    broke_cache.consumeTask()
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    logger.error(ex.message);
                }
            }
        }
    }
}