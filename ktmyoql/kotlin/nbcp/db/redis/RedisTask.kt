package nbcp.db.redis

import nbcp.comm.AsLong
import nbcp.comm.HasValue
import nbcp.db.cache.CacheForBroke
import nbcp.model.MasterAlternateStack
import nbcp.utils.SpringUtil
import org.springframework.data.redis.core.RedisTemplate
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

object RedisTask {
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


    private val redisTemplate by lazy {
        return@lazy SpringUtil.getBean<RedisTemplate<String, Any>>()
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
            var list = redisTemplate.scan(pattern);
            if (list.any() == false) {
                break;
            }

            redisTemplate.delete(list);
        }

        pattern = "sc:*[${it.table}]*"

        for (i in 0..99) {
            var list = redisTemplate.scan(pattern);
            if (list.any() == false) {
                break;
            }

            redisTemplate.delete(list);
        }
    }

    private var thread = thread(start = true, isDaemon = true, name = "MyOqlRedisTask") {
        while (true) {
            Thread.sleep(1000)
            renewal_cache.consumeTask()
            broke_cache.consumeTask()
        }
    }
}