package nbcp

import nbcp.base.extend.FieldTypeJsonMapper
import nbcp.base.extend.GetSetTypeJsonMapper
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
        var c1 = c();

        var json1 = FieldTypeJsonMapper.instance.writeValueAsString(c1);
        println(json1);


        var json2 = GetSetTypeJsonMapper.instance.writeValueAsString(c1);
        print(json2)
    }
}