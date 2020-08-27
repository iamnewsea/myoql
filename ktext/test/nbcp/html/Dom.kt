package nbcp.html

import nbcp.TestBase
import org.junit.Test




fun html(init: String.() -> String): String {
    var html = "";
    return html.init()
}


class DomTest:TestBase(){
    @Test
    fun test(){
        var d = html {
             "www";
        }

        println(d)
    }

}