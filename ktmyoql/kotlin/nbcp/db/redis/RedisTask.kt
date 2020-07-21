package nbcp.db.redis

import nbcp.comm.AsInt
import nbcp.comm.AsLong
import nbcp.comm.minus
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
        private var masterWording = false;
        private val masterKeys = linkedMapOf<String, Int>()
        private val alternateKeys = linkedMapOf<String, Int>()


        private var lastExecuteTime = LocalDateTime.now();

        /**
         * 定时任务，使键续期。
         */
        fun setExpireKey(key: String, cacheSecond: Int) {
            if (masterWording) {
                alternateKeys.put(key, cacheSecond);
            } else {
                masterKeys.put(key, cacheSecond);
            }
        }

        fun deleteKeys(vararg keys: String) {
            keys.forEach {
                masterKeys.remove(it);
                alternateKeys.remove(it);
            }
        }
    }

    /**
     * 定期执行任务是每秒，但是还是可以额外的定义休息时间。
     */
    @Scheduled(fixedDelay = 1000)
    fun renewal() {
        masterWording = !masterWording;
        var rKeys = linkedMapOf<String, Int>();

        try {
            if (masterWording) {
                rKeys = masterKeys
            } else {
                rKeys = alternateKeys;
            }

            if (SpringUtil.isInited == false) {
                return;
            }

            var redisTaskSize = SpringUtil.context.environment.getProperty("app.redis.task.size").AsInt(1024);
            var redisTaskDelay = SpringUtil.context.environment.getProperty("app.redis.task.delay").AsInt(15);

            var working = rKeys.size > redisTaskSize || LocalDateTime.now().minus(lastExecuteTime).totalSeconds > redisTaskDelay;

            if (working == false) {
                return;
            }

            lastExecuteTime = LocalDateTime.now();
            var keyCommand = SpringUtil.getBean<AnyTypeRedisTemplate>();

            rKeys.keys.forEach { key ->
                var cacheSecond = rKeys[key];

                if (cacheSecond == null || cacheSecond <= 0) {
                    return@forEach
                }

                keyCommand.expire(key, cacheSecond.AsLong(), TimeUnit.SECONDS)
            }

            if (masterWording) {
                masterKeys.clear()
            } else {
                alternateKeys.clear();
            }
        } catch (e: Exception) {
            logger.error(e.message, e);
        }
    }
}