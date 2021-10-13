package nbcp.redis

import nbcp.TestBase
import nbcp.db.redis.scanKeys
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component

@Component
class testa : TestBase() {

    @Autowired
    lateinit var template: StringRedisTemplate

    @Test
    fun testScanKeys() {
        template.scanKeys("sys:*") {
            println(it)
            return@scanKeys true;
        }
    }
}