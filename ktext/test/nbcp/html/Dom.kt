package nbcp.html

import nbcp.TestBase
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