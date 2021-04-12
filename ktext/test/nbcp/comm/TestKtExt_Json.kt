package nbcp.comm

import nbcp.*
import nbcp.db.*
import org.junit.Test

class TestKtExt_Json : TestBase() {

    class bc {
        var isDeleted: Boolean? = false;
        var list = mutableListOf<IdName>();
        var ary = arrayOf<IdName>()
    }

    @Test
    fun test_ToJson() {
        var b = bc();
        b.list.add(IdName("1", "abc"))
        b.ary = arrayOf(IdName("2", "def"))
        b.isDeleted = true;

        usingScope(JsonStyleEnumScope.GetSetStyle) {
            println(b.ToJson())
        }

        usingScope(JsonStyleEnumScope.FieldStyle) {
            println(b.ToJson())
        }
    }

    @Test
    fun test_FromJson() {
        var b = bc();
        b.list.add(IdName("1", "abc"))
        b.ary = arrayOf(IdName("2", "def"))
        b.isDeleted = true;


        var result   = ApiResult<Any>();
        result.data = b;

        var str = result.ToJson();
        usingScope(JsonStyleEnumScope.GetSetStyle) {
            result = str.FromJson<ApiResult<Any>>()!!;
            println(result.data!!.ConvertJson(bc::class.java).list.first().ToJson())
        }
    }
}