package nbcp

import nbcp.comm.*
import nbcp.db.IdUrl
import org.junit.jupiter.api.Test
import java.io.BufferedReader
import java.io.InputStreamReader
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import nbcp.scope.*

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
        usingScope(JsonSceneEnumScope.Web) {
            var a = IdUrl();
            a.url = "OK"
            println(a.ToJson())
        }

        usingScope(JsonSceneEnumScope.Db) {
            var a = IdUrl();
            a.url = "OK"
            println(a.ToJson())
        }
    }

    @Test
    fun abcd() {
        var clss = ByteArray::class.java
        println(clss.typeName)
    }

    @Test
    fun abcd2() {
        println(65.toChar())
    }

    @Test
    fun rou() {
        var line = execCmd("cmd", "/c", " dirw d:")

        line.data.forEach {
            println(it)
        }
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
                    return ListResult(lines.joinToString(","))
                }
            }
        } catch (e: Exception) {
            return ListResult(e.message ?: "error")
        }
    }
}