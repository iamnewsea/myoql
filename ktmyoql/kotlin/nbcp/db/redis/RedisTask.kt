package nbcp.db.redis

import nbcp.comm.AsInt
import nbcp.comm.AsLong
import nbcp.comm.config
import nbcp.comm.minus
import nbcp.model.MasterAlternateMap
import nbcp.utils.*
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.DependsOn
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.lang.Exception
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

/**
 * 使用缓存的方式，把要续期的键集中起来，每10秒执行一次续期。
 * 用处是用户的token，用户每次访问都应该使用缓存的方式续期。
 */
@Service
@DependsOn("springUtil")
@ConditionalOnProperty("spring.redis.host")
class RedisTask {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
        /**
         * 续期的 keys，value=过期时间，单位秒
         */
        private var cache = MasterAlternateMap<Int>();

        /**
         * 定时任务，使键续期。
         */
        fun setRenewalKey(key: String, cacheSecond: Int) {
            cache.put(key, cacheSecond);
        }

        fun deleteKeys(vararg keys: String) {
            keys.forEach {
                cache.removeAll(it);
            }
        }
    }

    /**
     * 定期执行任务是每秒，但是还是可以额外的定义休息时间。
     */
    @Scheduled(fixedDelay = 1000)
    fun renewal() {
        if (SpringUtil.isInited == false) {
            return;
        }

        try {
            var redisTaskSize = config.redisTaskSize;
            var redisTaskDelay = config.redisTaskDelay;

            var working = cache.reservoirSize > redisTaskSize || LocalDateTime.now().minus(cache.switchAt).totalSeconds > redisTaskDelay;

            if (working == false) {
                return;
            }

            cache.switch();
            var keyCommand = SpringUtil.getBean<AnyTypeRedisTemplate>();

            while (true) {
                var item = cache.pop()
                if (item == null) {
                    break;
                }

                var key = item.first;
                var cacheSecond = item.second;

                if (cacheSecond == null || cacheSecond <= 0) {
                    break;
                }

                keyCommand.expire(key, cacheSecond.AsLong(), TimeUnit.SECONDS)
            }
        } catch (e: Exception) {
            logger.error(e.message, e);
        }
    }
}