package nbcp.base

import nbcp.base.comm.ListResult
import nbcp.base.db.IdUrl
import nbcp.base.enums.JsonSceneScopeEnum
import nbcp.base.extend.AsString
import nbcp.base.extend.ToJson
import nbcp.base.extend.Xml2Json
import nbcp.base.extend.usingScope
import nbcp.base.utils.MyUtil
import org.junit.jupiter.api.Test
import java.io.BufferedReader
import java.io.InputStreamReader
import java.time.LocalDate
import java.time.LocalDateTime
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
        usingScope(JsonSceneScopeEnum.WEB) {
            var a = IdUrl();
            a.url = "OK"
            println(a.ToJson())
        }

        usingScope(JsonSceneScopeEnum.DB) {
            var a = IdUrl();
            a.url = "OK"
            println(a.ToJson())
        }
    }

    @Test
    fun abcd() {
        for (i in 1 until 1000) {
            var now = LocalDateTime.now();
            println(now.AsString() + "  " + System.nanoTime())
        }
    }

    @Test
    fun abcd2() {
//        var a = const.utf8.encode("上").toByteArray().map { it.toUByte().toString(16) }
//        println(a)
        println(MyUtil.decodeStringFromFanOctalCode("\\344\\270\\212\\347\\272\\277"))
    }

    fun execCmd(vararg cmds: String): ListResult<String> {
        var p = Runtime.getRuntime().exec(cmds);
        var lines = listOf<String>()

        try {
            p.waitFor()
            if (p.exitValue() == 0) {
                BufferedReader(InputStreamReader(p.inputStream, "GBK")).use { br ->
                    lines = br.readLines()
                    return ListResult.of(lines)
                }
            } else {
                BufferedReader(InputStreamReader(p.errorStream, "GBK")).use { br ->
                    lines = br.readLines();
                    return ListResult.error(lines.joinToString(","))
                }
            }
        } catch (e: Exception) {
            return ListResult.error(e.message ?: "error")
        }
    }
}