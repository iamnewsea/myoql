package nbcp.extend

import nbcp.TestBase
import nbcp.comm.AsString
import nbcp.comm.ConvertToLocalDateTime
import nbcp.comm.Format
import nbcp.comm.ToReadableAmountValue
import org.junit.Test
import java.time.format.DateTimeFormatter

class TestConvert :TestBase(){
    @Test
    fun testFormat(){
        var d = 23.4;
        println(d.Format("#0.00%"))
    }

    @Test
    fun testFormat2(){
        var d = 1234123456789L;
        println(d.ToReadableAmountValue())
    }

    @Test
    fun test_type_convert() {
        println("20111203101530".ConvertToLocalDateTime(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")))
        //println(ConvertToLocalDateTime("2011-12-03T10:15:30Z", DateTimeFormatter.ISO_INSTANT))
    }
}