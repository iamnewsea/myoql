package nbcp

import nbcp.base.extend.FieldTypeJsonMapper
import nbcp.base.extend.GetSetTypeJsonMapper
import nbcp.db.IdUrl
import org.junit.Test

class testa : TestBase() {

    class c {
        var id: String = "";
        var isAdmin:Boolean = false;

        fun getName(): String {
            return "ok"
        }
    }

    @Test
    fun abc() {
        var url = c();

        var json1 = FieldTypeJsonMapper.instance.writeValueAsString(url);
        println(json1);


        var json2 = GetSetTypeJsonMapper.instance.writeValueAsString(url);
        print(json2)

        var json3 = GetSetTypeJsonMapper.instance.readValue("""{"id":"","name":"ok","isAdmin":true}""",c::class.java);
        println(json3.isAdmin)
    }
}