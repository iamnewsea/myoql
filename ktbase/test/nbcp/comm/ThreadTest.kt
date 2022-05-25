package nbcp.comm

import nbcp.TestBase
import nbcp.db.IdName
import nbcp.db.LoginUserModel
import nbcp.extend.ToYaml
import nbcp.scope.JsonSceneEnumScope
import nbcp.utils.RecursionUtil
import nbcp.utils.SpringUtil
import org.junit.jupiter.api.Test
import java.io.Serializable
import java.time.LocalDateTime
import kotlin.concurrent.thread

class ThreadTest : TestBase() {

    @Test
    fun test_thread() {
        var result = thread(start = false) {
            while (true) {
                Thread.sleep(3000)
            }
        }

        println(result.isAlive)
        if (result.isAlive == false) {
            result.start()
        }
        println(result.isAlive)
    }
}