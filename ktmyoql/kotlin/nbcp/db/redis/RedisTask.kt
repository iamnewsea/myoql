package nbcp.db.redis

import nbcp.base.extend.AsInt
import nbcp.base.extend.AsLong
import nbcp.base.utils.SpringUtil
import org.slf4j.LoggerFactory
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.StringRedisConnection
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.lang.Exception
import java.util.concurrent.ConcurrentHashMap

@Service
class RedisTask {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
        /**
         * 续期的 keys，value=过期时间，单位秒
         */
        private var masterWording = false;
        private val renewalKeys = linkedMapOf<String, Int>()
        private val renewalKeys_alternate = linkedMapOf<String, Int>()

        private val keyCommand: StringRedisConnection by lazy {
            return@lazy SpringUtil.getBean<StringRedisConnection>()
        }

        /**
         * 定时任务，使键续期。
         */
        fun setExpireKey(key: String, cacheSecond: Int) {
            if (masterWording) {
                renewalKeys_alternate.put(key, cacheSecond);
            } else {
                renewalKeys.put(key, cacheSecond);
            }
        }
    }

    @Scheduled(fixedDelay = 5000)
    fun renewal() {
        masterWording = !masterWording;
        Thread.sleep(100);
        try {
            var rKeys = linkedMapOf<String, Int>();

            if (masterWording) {
                rKeys = renewalKeys
            } else {
                rKeys = renewalKeys_alternate;
            }

            rKeys.keys.forEach { key ->
                var cacheSecond = rKeys[key] ;

                if (cacheSecond == null || cacheSecond <= 0) {
                    return@forEach
                }

                keyCommand.expire(key, cacheSecond.AsLong())

//                redis.byteArrayCommand(dbOffset) {
//                    //是否需要判断键存在呢？
//                    it.expire(key, cacheSecond.AsLong())
//                }
            }

            if (masterWording) {
                renewalKeys.clear()
            } else {
                renewalKeys_alternate.clear();
            }
        } catch (e: Exception) {
            logger.error(e.message, e);
        }
    }
}