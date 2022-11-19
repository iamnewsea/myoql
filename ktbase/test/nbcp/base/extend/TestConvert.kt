package nbcp.base.extend

import nbcp.base.TestBase
import nbcp.base.comm.JsonMap
import nbcp.base.enums.JsonStyleScopeEnum
import nbcp.base.utils.CookieData
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class TestConvert : TestBase() {
    @Test
    fun testNumberFormat() {
        val d = 23.4;
        println(d.Format("#0.00%"))
    }

    @Test
    fun testList() {
        var list = "a,b,c,---,d,e,f,---,x,y,z".split(",")
            .SplitList {
                it == "---"
            };

        println(list.ToJson())
    }

    @Test
    fun testNumberAmountFormat() {
        val d = 1234123456789L;
        println(d.ToReadableAmountValue())
    }

    @Test
    fun testStringFormat() {
        val d = "{i} love {you}, in {flow[1]} , id:{f[1].id}";
        println(
            d.formatWithJson(
                JsonMap(
                    "i" to "郭",
                    "you" to "黄",
                    "flow" to arrayOf("天上", "人间"),
                    "f" to arrayOf("天上", JsonMap("id" to "111"))
                )
            )
        )
    }

    @Test
    fun timeConvert() {
        val now = LocalDateTime.now()

        println(now.Format(listOf(JsonStyleScopeEnum.DATE_UTC_STYLE).getDateFormat()))
        println(now.Format(listOf(JsonStyleScopeEnum.DATE_STANDARD_STYLE).getDateFormat()))
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
        val d = LocalDateTime.parse("Fri, 04 Dec 2020 03:11:42 GMT", DateTimeFormatter.RFC_1123_DATE_TIME);
        println(d.AsString())


        val values =
            """_csrf=;Path=/; Max-Age=0; HttpOnly,lang=zh-CN; Path=/; HttpOnly,_csrf=FRWONyVi4NswjAuwH8WFRdZbA7k6MTYwNjk2NTEwMjg0ODI1OTEwMA; Path=/; Expires=Fri, 04 Dec 2020 03:11:42 GMT; HttpOnly,i_like_gitea=27ad2c25c735ad2d; Path=/; HttpOnly,lang=en-US; Path=/; Max-Age=2147483647"""

        CookieData.parse(values).forEach {
            println(it.toString())
        }
    }
}