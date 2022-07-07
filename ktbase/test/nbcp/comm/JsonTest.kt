package nbcp.comm

import nbcp.TestBase
import nbcp.db.IdName
import nbcp.db.LoginUserModel
import nbcp.extend.ToYaml
import nbcp.scope.JsonSceneEnumScope
import nbcp.utils.RecursionUtil
import nbcp.utils.SpringUtil
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
        var map = JsonMap();
        map.put("ok", 12)
        map.put("ok22", JsonMap("ff" to 1));

        println("""{"id":1,"name":null}""".FromJson<JsonMap>()!!.keys)
        println(map.keys)

        var _r: ((Map<String, Any?>) -> Unit)? = null;
        var r: ((Map<String, Any?>) -> Unit) = {
            println("m:" + it.ToJson())

            it.values.filter { it is Map<*, *> }.map { it as Map<String, Any?> }.forEach {
                _r!!(it);
            }
        }
        _r = r;

        r(map);

        RecursionUtil.recursionJson(map, {
            println("rm:" + it.ToJson())
            return@recursionJson true;
        })
    }


    @Test
    fun test_list_json() {
        var txt = """
logging:
  level:
    root: WARN
    com:
      mongodb: TRACE
    org:
      springframework:
        data:
          mongodb: TRACE
        security: WARN
    nbcp:
      MainApplication: TRACE
      client: DEBUG
      base:
        filter: INFO
      db:
        mongo: TRACE
        redis: INFO
        mysql: INFO
        mq: INFO
    sample:
      mybatis:
        mapper: TRACE
"""

        var txt2 = """
logging:
  config: classpath:logback-skywalking.xml
  file:
    path: logs
  level:
    root: WARN
    nbcp:
      base:
        filter: WARN
      utils:
        HttpUtil: INFO
        """

        var map = txt.FromYamlText<JsonMap>();
        var map2 = txt2.FromYamlText<JsonMap>()
        println(map.ToJson())
        println(map2.ToJson())


        println("=================")
        println(map.deepJoin(map2).ToJson())
        println(map.deepJoin(map2).ToYaml())
        println("=================")
    }

    @Test
    fun ffff() {
        var l = LoginUserModel();
        l.loginName = "OK"
        l.groups = listOf("a", "b")

        println("==============")
        println(SpringUtil.context.simpleFieldToJson())
        println("==============")
        println(SpringUtil.context.simpleFieldToJson(2))
        println("==============")
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