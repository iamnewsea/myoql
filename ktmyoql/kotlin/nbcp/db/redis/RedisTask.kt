package nbcp.db.redis

import nbcp.comm.AsLong
import nbcp.comm.config
import nbcp.model.MasterAlternateStack
import nbcp.utils.*
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration
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
@ConditionalOnBean(RedisAutoConfiguration::class)
class RedisTask {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)

        /**
         * 续期的 keys，value=过期时间，单位秒
         */
        private var cache = MasterAlternateStack<Pair<String, Int>>() {
            var keyCommand = SpringUtil.getBean<AnyTypeRedisTemplate>();

            var key = it.first;
            var cacheSecond = it.second;

            if (cacheSecond <= 0) {
                return@MasterAlternateStack
            }

            keyCommand.expire(key, cacheSecond.AsLong(), TimeUnit.SECONDS)
        };

        /**
         * 定时任务，使键续期。
         */
        fun setRenewalKey(key: String, cacheSecond: Int) {
            cache.push(key to cacheSecond);
        }

        fun deleteKeys(vararg keys: String) {
            keys.forEach {
                cache.removeAll(it);
            }
        }
    }
}