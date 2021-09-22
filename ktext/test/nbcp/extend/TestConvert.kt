package nbcp.extend

import nbcp.TestBase
import nbcp.comm.*
import nbcp.utils.CookieData
import org.junit.Test
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import nbcp.scope.*
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
    fun timeConvert() {
        var now = LocalDateTime.now()

        println(now.Format(listOf(JsonStyleEnumScope.DateUtcStyle).getDateFormat()))
        println(now.Format(listOf(JsonStyleEnumScope.DateStandardStyle).getDateFormat()))
    }

    @Test
    fun test_type_convert() {
//        println("20111203101530".ConvertToLocalDateTime(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")))
//
//        println("2021-03-05T08:43:18.986Z".ConvertToLocalDateTime())
//        println("Fri, 05 Mar 2021 07:46:57 GMT".ConvertToLocalDateTime(DateTimeFormatter.RFC_1123_DATE_TIME))
        println("2021-03-05T15:45:14+07:00".ConvertToLocalDateTime())
    }

    @Test
    fun test_type2_convert() {
        var d = LocalDateTime.parse("Fri, 04 Dec 2020 03:11:42 GMT", DateTimeFormatter.RFC_1123_DATE_TIME);
        println(d.AsString())


        var values =
            """_csrf=;Path=/; Max-Age=0; HttpOnly,lang=zh-CN; Path=/; HttpOnly,_csrf=FRWONyVi4NswjAuwH8WFRdZbA7k6MTYwNjk2NTEwMjg0ODI1OTEwMA; Path=/; Expires=Fri, 04 Dec 2020 03:11:42 GMT; HttpOnly,i_like_gitea=27ad2c25c735ad2d; Path=/; HttpOnly,lang=en-US; Path=/; Max-Age=2147483647"""

        CookieData.parse(values).forEach {
            println(it.toString())
        }
    }
}