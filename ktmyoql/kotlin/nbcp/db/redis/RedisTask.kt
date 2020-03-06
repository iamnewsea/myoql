package nbcp.db.redis

import nbcp.base.extend.AsInt
import nbcp.base.extend.AsLong
import nbcp.base.utils.SpringUtil
import org.slf4j.LoggerFactory
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
        private val renewalKeys = linkedMapOf<String, String>()
        private val renewalKeys_alternate = linkedMapOf<String, String>()


        protected val redis by lazy {
            return@lazy SpringUtil.getBean<RedisConfig>()
        }

        /**
         * 定时任务，使键续期。
         */
        fun setExpireKey(key: String, dbOffset: Int, cacheSecond: Int) {
            if (masterWording) {
                renewalKeys_alternate.put(key, "${dbOffset - cacheSecond}");
            } else {
                renewalKeys.put(key, "${dbOffset - cacheSecond}");
            }
        }
    }

    @Scheduled(fixedDelay = 5000)
    fun renewal() {
        masterWording = !masterWording;
        Thread.sleep(100);
        try {
            var rKeys = linkedMapOf<String, String>();

            if (masterWording) {
                rKeys = renewalKeys
            } else {
                rKeys = renewalKeys_alternate;
            }

            rKeys.keys.forEach { key ->
                var sects = rKeys[key]!!.split("-");
                var dbOffset = sects[0].AsInt();
                var cacheSecond = sects[1].AsInt();

                if (cacheSecond <= 0) {
                    return@forEach
                }
                redis.byteArrayCommand(dbOffset) {
                    //是否需要判断键存在呢？
                    it.expire(key, cacheSecond.AsLong())
                }
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