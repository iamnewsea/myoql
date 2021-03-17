package nbcp.redis

import nbcp.TestBase
import nbcp.comm.AllFields
import nbcp.comm.Define
import nbcp.db.*
import nbcp.db.es.IEsDocument
import nbcp.db.es.tool.generator_mapping
import nbcp.tool.UserCodeGenerator
import nbcp.utils.SpringUtil
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class testa : TestBase() {

    @Autowired
    lateinit var template: RedisTemplate<String,String>
    @Test
    fun abc() {
        template.opsForValue().set("a","a")
    }
}