package nbcp.comm

import nbcp.TestBase
import nbcp.comm.*
import nbcp.utils.CookieData
import org.junit.Test
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter


open class base{
    companion object{
          var list = mutableListOf<String>()

        fun add(value:String){
            this.list .add(value);
        }
    }
}

class child:base(){
    companion object{
          var list = mutableListOf<String>()

        fun add(value:String){
            this.list .add(value);
        }
    }
}
class TypeTest : TestBase() {

    @Test
    fun abc(){
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
}