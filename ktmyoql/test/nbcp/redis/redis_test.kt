package nbcp.redis

import nbcp.TestBase
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component

@Component
class testa : TestBase() {

    @Autowired
    lateinit var template: RedisTemplate<String, String>

    @Test
    fun abc() {
        template.opsForValue().set("a", "a")
    }
}