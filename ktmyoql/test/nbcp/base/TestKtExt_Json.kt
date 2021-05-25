package nbcp.base

import nbcp.TestBase
import nbcp.comm.*
import nbcp.db.CityCodeName
import org.junit.Test

class TestKtExt_Json : TestBase() {


    @Test
    fun test_FromJson() {
        usingScope(JsonSceneEnumScope.Web) {
            println("""{"code":123}""".FromJson<CityCodeName>()!!.name)
        }

        usingScope(JsonSceneEnumScope.Db) {
            println("""{"code":123}""".FromJson<CityCodeName>()!!.name)
        }
    }
}