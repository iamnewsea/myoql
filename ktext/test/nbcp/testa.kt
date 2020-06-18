package nbcp

import ch.qos.logback.classic.Level
import nbcp.comm.*
import nbcp.utils.*
import nbcp.comm.*
import nbcp.utils.*
import nbcp.db.IdName
import nbcp.db.IdUrl
import org.junit.Test
import org.springframework.stereotype.Service
import java.io.BufferedReader
import java.io.InputStreamReader
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class testa : TestBase() {

    @Test
    fun ww() {
        var xml = """
<xml>
    <return_code> <![CDATA[ SUCCESS ]]></return_code> 
    <return_msg><![CDATA[OK]]></return_msg> 
    <appid><![CDATA[wxd5f2957442fc2d0b]]></appid> 
    <mch_id><![CDATA[1575431351]]></mch_id> 
    <device_info><![CDATA[WEB]]></device_info>
    <nonce_str><![CDATA[Qf4qjLL40q2JETd4]]></nonce_str>
    <sign><![CDATA[AEB98FC46C783E32BD9359720006FEF8]]></sign>
    <result_code><![CDATA[SUCCESS]]></result_code>
    <prepay_id><![CDATA[wx22134704285795ae65e3aa831098894500]]></prepay_id>
    <trade_type><![CDATA[JSAPI]]></trade_type>
</xml>
"""

        println(xml.Xml2Json().ToJson())
        println("".Xml2Json().ToJson())


    }

    data class abc(
            var t: LocalDateTime = LocalDateTime.now(),
            var i: Date = Date(),
            var l: LocalDate = LocalDate.now()
    )

    @Test
    fun test_IdUrl_Json() {
        using(JsonStyleEnumScope.GetSetStyle) {
            var a = IdUrl();
            a.url = "OK"
            println(a.ToJson())
        }

        using(JsonStyleEnumScope.FieldStyle) {
            var a = IdUrl();
            a.url = "OK"
            println(a.ToJson())
        }
    }

    @Test
    fun abcd() {
        println("2020/06/16 20:05".AsLocalDate())
        println("2020/06/16 20:05:06.888".AsLocalDateTime())
        println("2020/06/16 20:05Z".AsLocalDateTime())
    }
    @Test
    fun abcd2() {
        println("0xFF".AsInt())
    }

    @Test
    fun rou() {
        var line = execCmd("cmd", "/c", " dirw d:")

        line.data.forEach {
            println(it)
        }
    }

    fun execCmd(vararg cmds: String): ListResult<String> {
        logger.warn(cmds.joinToString(" "));
        var p = Runtime.getRuntime().exec(cmds);
        var sb = StringBuilder();
        var lines = listOf<String>()

        var br: BufferedReader? = null;
        try {
            p.waitFor()
            if (p.exitValue() == 0) {
                br = BufferedReader(InputStreamReader(p.inputStream, "GBK"));
                lines = br.readLines()
                return ListResult.of(lines)
            } else {
                br = BufferedReader(InputStreamReader(p.errorStream, "GBK"));
                lines = br.readLines();
                return ListResult(lines.joinToString(","))
            }
        } catch (e: Exception) {
            return ListResult(e.message ?: "error")
        } finally {
            if (br != null) {
                try {
                    br.close();
                } finally {
                }
            }
        }
    }
}