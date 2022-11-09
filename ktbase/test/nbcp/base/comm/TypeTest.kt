package nbcp.base.comm

import nbcp.base.TestBase
import nbcp.base.extend.ToJson
import nbcp.base.extend.must
import org.junit.jupiter.api.Test
import java.time.Duration


open class base {
    companion object {
        var list = mutableListOf<String>()

        fun add(value: String) {
            this.list.add(value);
        }
    }
}

class child : base() {
    companion object {
        var list = mutableListOf<String>()

        fun add(value: String) {
            this.list.add(value);
        }
    }
}

class TypeTest : TestBase() {

    @Test
    fun abc() {
        child.add("K")
        print(base.list.ToJson())
    }


    @Test
    fun test_type2_convert() {
        println(TypeTest::class.isFun)
    }

    @Test
    fun test_time() {
        // seconds == totalSeconds
        println(Duration.ofDays(3).seconds)
    }


    @Test
    fun test_str() {
        // seconds == totalSeconds
        println(abcfff())
    }

    fun abcfff(): String {
        var d: String? = null;

        d.must().elseReturn { return "nulllll" }

        return "proced"
    }
}