package nbcp.redis

import nbcp.TestBase
import nbcp.db.db
import nbcp.db.redis.scanKeys
import nbcp.utils.SpringUtil
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.concurrent.TimeUnit

@Component
class testa : TestBase() {

    @Autowired
    lateinit var template: StringRedisTemplate

    @Test
    fun testScanKeys() {
        template.scanKeys("mp:*ip*") {
            println(it)
            return@scanKeys true;
        }
    }

    @Test
    fun Scan() {
        var t = SpringUtil.getBean<StringRedisTemplate>();
        var key = "def"

        t.opsForValue().set(key, "!!abc!!", Duration.ofHours(4))
        println(t.opsForValue().get(key))
        println("expire:" + t.getExpire(key, TimeUnit.MINUTES))
        t.expire(key, Duration.ofHours(1)).apply {
            println(this)
        }
        println("expire:" + t.getExpire(key, TimeUnit.MINUTES))
    }

    @Test
    fun testSetNx() {
        var d = db.rer_base.taskLock.setIfAbsent("abc", "ffff")
        println(d)
    }
}