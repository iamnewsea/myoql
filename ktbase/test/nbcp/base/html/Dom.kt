package nbcp.base.html

import nbcp.base.TestBase
import org.junit.jupiter.api.Test


fun html(init: String.() -> String): String {
    return "".init()
}


class DomTest : TestBase() {
    @Test
    fun test() {
        val d = html {
            "www";
        }

        println(d)
    }

}