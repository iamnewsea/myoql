package nbcp.comm

import nbcp.*
import nbcp.db.*
import org.junit.jupiter.api.Test
import nbcp.scope.*

class TestKtExt_Json : TestBase() {

    class bc {
        var isDeleted: Boolean? = false;
        var list = mutableListOf<IdName>();
        var ary = arrayOf<IdName>()

        fun getAbc(): String {
            return if (isDeleted == true) "dddd" else "nnnn"
        }
    }

    @Test
    fun test_ToJson() {
        var b = bc();
        b.list.add(IdName("1", "abc"))
        b.ary = arrayOf(IdName("2", "def"))
        b.isDeleted = true;

        usingScope(JsonSceneEnumScope.Web) {
            println(b.ToJson())
        }
        println(b.ToJson(JsonSceneEnumScope.Web))

        usingScope(JsonSceneEnumScope.Db) {
            println(b.ToJson())
        }
        println(b.ToJson(JsonSceneEnumScope.Db))
    }

    @Test
    fun test_FromJson() {
        var b = bc();
        b.list.add(IdName("1", "abc"))
        b.ary = arrayOf(IdName("2", "def"))
        b.isDeleted = true;


        var result = ListResult<bc>();
        result.data = listOf(b);

        var str = result.ToJson();
        usingScope(JsonSceneEnumScope.Web) {
            var r2 = str.FromJson<ListResult<bc>>()!!;
            println(r2.data!!.first()::class.java.name)

            var r3 = str.FromGenericJson(ListResult::class.java,IdName::class.java)!!
            println(r3.data.first()!!::class.java.name)
        }


        var list = listOf(b)
        str = list.ToJson()

        var d = str.FromGenericJson(List::class.java,bc::class.java)!!;
        println(d.first()!!::class.java.name)
    }

    class b{
        var c = ""
    }
    class a{
        var b = b();
    }
    class ccc{
        var a = a()
    }

    @Test
    fun abc(){
        var map = JsonMap("a.b.c" to "ok")
        var e = map.ConvertJson(ccc::class.java)
        println(e.a.b.c)
    }
}