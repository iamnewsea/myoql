package nbcp.myoql.db.redis

import nbcp.base.extend.AsLong
import nbcp.base.extend.Important
import nbcp.base.model.DualPoolMap
import nbcp.base.utils.SpringUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.data.redis.core.StringRedisTemplate
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

@ConditionalOnClass(StringRedisTemplate::class)
class RedisRenewalDynamicService : InitializingBean {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)

        /**
         * 定时任务，使键续期。
         */
        fun setDelayRenewalKey(key: String, cacheSecond: Int) {
            renewal_cache.inputPool.put(key, cacheSecond);
        }

        fun clearDelayRenewalKeys(vararg keys: String) {
            keys.forEach {
                renewal_cache.removeAllItem(it);
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
        private var renewal_cache = DualPoolMap<String, Int>()
            .consumer { key, cacheSecond ->
                if (cacheSecond <= 0) {
                    return@consumer
                }

                var ret = redisTemplate.expire(key, cacheSecond.AsLong(), TimeUnit.SECONDS)
                if (ret == false) {
                    println("设置过期 ${key} 失败")
                }
            }


        private val task = thread(start = false, isDaemon = true, name = "RedisRenewalTask") {
            while (true) {
                Thread.sleep(3000)
                try {
                    renewal_cache.consumeTask()
                } catch (ex: Throwable) {
                    logger.error("Redis续期线程出错,6秒后再试", ex);
                }
                Thread.sleep(3000)
            }
        }
    }


    /**
     * 每10秒同步一次。
     */
    override fun afterPropertiesSet() {
        if (task.isAlive == false) {
            logger.Important("""
~~-~~-~~-~~  Redis续期服务已启动! ~~-~~-~~-~~ 
""")
            task.start()
        }
    }
}