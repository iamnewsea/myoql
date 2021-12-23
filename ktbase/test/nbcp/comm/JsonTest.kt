package nbcp.comm

import nbcp.TestBase
import nbcp.db.IdName
import nbcp.scope.JsonSceneEnumScope
import org.junit.jupiter.api.Test
import java.io.Serializable
import java.time.LocalDateTime

class JsonTest : TestBase() {

    data class abc(var name: String) {
        val fullName: String
            get() {
                return this.name + "!"
            }

        var r: MyRawString = MyRawString();
    }

    class TestObj : Serializable {
        var n = ""

        @Transient
        var d = ""

        var creatAt = LocalDateTime.now();
    }

    @Test
    fun test_get_json() {
        var d = TestObj();
        d.n = "OK";
        d.d = "ee"

        println(d.CloneObject().ToJson())
    }


    @Test
    fun test_list_json() {
        var d = listOf(TestObj())
        d[0].n = "OK";
        d[0].d = "ee"

        println(d.ToJson().FromListJson(TestObj::class.java).ToJson())
    }


    class ResultVO(var users: List<IdName>)


    @Test
    fun test_vo_list_json() {
        var result = ResultVO(listOf(IdName("1", "abc"), IdName("2", "def")));
        var listResult = ListResult.of(listOf(result));

        var v2 = usingScope(JsonSceneEnumScope.App) {
            listResult.ToJson().FromJson<ListResult<ResultVO>>()!!;
        }
        v2.data.resetListItemType(ResultVO::class.java)

        println(v2.data[0].users[0].name)
    }
}