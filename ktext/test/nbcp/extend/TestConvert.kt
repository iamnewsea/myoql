package nbcp.extend

import nbcp.TestBase
import nbcp.comm.*
import org.junit.Test
import java.time.format.DateTimeFormatter

class TestConvert : TestBase() {
    @Test
    fun testNumberFormat() {
        var d = 23.4;
        println(d.Format("#0.00%"))
    }

    @Test
    fun testNumberAmountFormat() {
        var d = 1234123456789L;
        println(d.ToReadableAmountValue())
    }

    @Test
    fun testStringFormat() {
        var d = "{i} love {you}";
        println(d.formatWithJson(StringMap("i" to "郭", "you" to "黄")))
    }

    @Test
    fun test_type_convert() {
        println("20111203101530".ConvertToLocalDateTime(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")))
        //println(ConvertToLocalDateTime("2011-12-03T10:15:30Z", DateTimeFormatter.ISO_INSTANT))
    }
}