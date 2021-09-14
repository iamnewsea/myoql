package nbcp.db.redis

import nbcp.comm.AsLong
import nbcp.db.cache.CacheForBrokeData
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
class RedisTask : InitializingBean {
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
        private var renewal_cache = MasterAlternateMap<String, Int>({ a, b -> Math.max(a, b) }) { key, cacheSecond ->
            if (cacheSecond <= 0) {
                return@MasterAlternateMap
            }

            redisTemplate.expire(key, cacheSecond.AsLong(), TimeUnit.SECONDS)
        };

        /**
         * 缓存破坏
         */
        private var broke_cache = MasterAlternateSet<CacheForBrokeData>() { cacheBroke ->
            var pattern = "sc:${cacheBroke.table}:*";

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

                //先移除不含 @ 的key
                var like_sql_keys = all_keys.filter { it.contains("@") == false };
                if (like_sql_keys.any()) {
                    redisTemplate.delete(like_sql_keys)
                }

                //破坏其它维度的分组
                var other_group_keys = all_keys.filter { it.contains(":${cacheBroke.key}@") == false };
                if (other_group_keys.any()) {
                    redisTemplate.delete(other_group_keys);
                }

                //再精准破坏 key,value分组的。
                var this_group_keys = all_keys.filter { it.contains(":${cacheBroke.key}@${cacheBroke.value}:") };

                if (this_group_keys.any()) {
                    redisTemplate.delete(this_group_keys);
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