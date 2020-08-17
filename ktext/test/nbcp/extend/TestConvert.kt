package nbcp.extend

import nbcp.TestBase
import nbcp.comm.ConvertToLocalDateTime
import org.junit.Test
import java.time.format.DateTimeFormatter

class TestConvert :TestBase(){
    @Test
    fun testFormat(){

    }



    @Test
    fun test_type_convert() {
        println("20111203101530".ConvertToLocalDateTime(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")))
        //println(ConvertToLocalDateTime("2011-12-03T10:15:30Z", DateTimeFormatter.ISO_INSTANT))
    }
}