package nbcp.base.comm

import nbcp.base.TestBase
import nbcp.base.comm.*
import nbcp.base.db.IdName
import nbcp.base.enums.JsonSceneScopeEnum
import nbcp.base.enums.JsonStyleScopeEnum
import nbcp.base.extend.*
import org.junit.jupiter.api.Test

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

        usingScope(JsonSceneScopeEnum.Web) {
            println(b.ToJson())
        }
        println(b.ToJson(JsonSceneScopeEnum.Web))

        usingScope(JsonSceneScopeEnum.Db) {
            println(b.ToJson())
        }
        println(b.ToJson(JsonSceneScopeEnum.Db))
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
        usingScope(JsonSceneScopeEnum.Web) {
            var r2 = str.FromJson<ListResult<bc>>()!!;
            println(r2.data!!.first()::class.java.name)

            var r3 = str.FromGenericJson(ListResult::class.java, IdName::class.java)!!
            println(r3.data.first()!!::class.java.name)
        }


        var list = listOf(b)
        str = list.ToJson()

        var d = str.FromGenericJson(List::class.java, bc::class.java)!!;
        println(d.first()!!::class.java.name)
    }

    class b {
        var c = ""
    }

    class a {
        var b = b();
    }

    class ccc {
        var a = a()
    }

    @Test
    fun abc() {
        var map = JsonMap("a.b.c" to "ok")
        var e = map.ConvertJson(ccc::class.java)
        println(e.a.b.c)
    }

    @Test
    fun withNul() {
        var map = JsonMap("a" to null)
        var e = map.ToJson(JsonStyleScopeEnum.WithNull)
        println(e)
    }


    @Test
    fun Any() {
        var obj1 = JsonMap("a" to 1).ToJson().FromJson<Any>()!!
        var obj2 = listOf(JsonMap("a" to 1)).ToJson().FromJson<Any>()!!
        println(obj1.javaClass.simpleName)
        println(obj2::class.java.simpleName)
    }
}