package nbcp.myoql.redis

import nbcp.myoql.TestBase
import nbcp.myoql.db.db
import nbcp.myoql.db.redis.scanAllKeys
import nbcp.myoql.db.redis.scanKeys
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component

@Component
class testa : TestBase() {

    @Autowired
    lateinit var template: StringRedisTemplate

    @Test
    fun testScanKeys() {
        template.scanAllKeys("*") {
            println(it)
            return@scanAllKeys true;
        }
    }

    @Test
    fun Scan() {
        var key = "127"
        db.rerBase.cityCodeName(key).set("localhost")
        Thread.sleep(15000);
        println("sleep5 , expire: ~ 3585 , " + db.rerBase.cityCodeName(key).getExpireSeconds())

        db.rerBase.cityCodeName(key).get()
        println("续期! expire: ~ 3585 , " + db.rerBase.cityCodeName(key).getExpireSeconds())

        Thread.sleep(1000)
        println("get, expire: ~ 3590 , " + db.rerBase.cityCodeName(key).getExpireSeconds())

        Thread.sleep(1000)
        println("get, expire: ~ 3580 , " + db.rerBase.cityCodeName(key).getExpireSeconds())
        println("done!")
    }

    @Test
    fun testSetNx() {
        var d = db.rerBase.taskLock("abc").setIfAbsent("ffff")
        println(d)
    }
}