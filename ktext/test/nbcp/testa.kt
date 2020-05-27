package nbcp

import ch.qos.logback.classic.Level
import nbcp.comm.*
import nbcp.utils.*
import nbcp.comm.*
import nbcp.utils.*
import nbcp.db.IdName
import nbcp.db.IdUrl
import org.junit.Test
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


    class abd {
        var www: LocalDateTime = LocalDateTime.now()
    }

    @Test
    fun abee() {

        for (i in 0..1000) {
            println(CodeUtil.getNumberValue())
        }
    }

    @Test
    fun abcd() {
        using(arrayOf(LogScope(Level.DEBUG_INT))) {
            var set = mutableSetOf<String>();
            set.add("c")
            set.add("a")
            set.add("b")
            set.add("c")
            set.add("a")
            set.add("b")

            println(set.elementAt(1))
        }

    }

    class jjj {
        var tags = mutableSetOf<String>()
    }

    @Test
    fun rou() {
        var j = jjj();
        j.tags.add("OK")

        println(j.ToJson().FromJson<jjj>().ToJson())
    }
}