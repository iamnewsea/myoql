package nbcp

import nbcp.base.extend.FieldTypeJsonMapper
import nbcp.base.extend.GetSetTypeJsonMapper
import nbcp.db.IdUrl
import org.junit.Test

class testa : TestBase() {

    class c {
        var id: String = "";

        fun getName(): String {
            return "ok"
        }
    }

    @Test
    fun abc() {
        System.setProperty("server.upload.host","http://dev8.cn:99")
        System.setProperty("server.upload.logoSize","256")

        var url = IdUrl("123","/logo.png")

        var json1 = FieldTypeJsonMapper.instance.writeValueAsString(url);
        println(json1);


        var json2 = GetSetTypeJsonMapper.instance.writeValueAsString(url);
        print(json2)
    }
}