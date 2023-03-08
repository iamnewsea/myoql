package nbcp.base.comm

import nbcp.base.TestBase
import org.junit.jupiter.api.Test


class CacheTest : TestBase() {

    @Test
    fun ab() {
        for (i in 1..5) {
            var v = MemoryCacheList.expireAfterAccess1Hour.get("OK") {
                println("--------------")
                return@get "OK"
            }

            println(v)


        }

        println(MemoryCacheList.expireAfterWrite4Hours.getIfPresent("OK"))
    }
}