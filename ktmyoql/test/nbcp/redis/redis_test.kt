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
        var key = "127"
        db.rer_base.cityCodeName(key).set("localhost")
        Thread.sleep(15000);
        println("sleep5 , expire: ~ 3585 , " + db.rer_base.cityCodeName(key).getExpireSeconds())

        db.rer_base.cityCodeName(key).get()
        println("续期! expire: ~ 3585 , " + db.rer_base.cityCodeName(key).getExpireSeconds())

        Thread.sleep(1000)
        println("get, expire: ~ 3590 , " + db.rer_base.cityCodeName(key).getExpireSeconds())

        Thread.sleep(1000)
        println("get, expire: ~ 3580 , " + db.rer_base.cityCodeName(key).getExpireSeconds())
        println("done!")
    }

    @Test
    fun testSetNx() {
        var d = db.rer_base.taskLock("abc").setIfAbsent("ffff")
        println(d)
    }
}