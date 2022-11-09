package nbcp.myoql.base

import nbcp.myoql.TestBase
import nbcp.base.enums.*;
import nbcp.base.extend.*;
import nbcp.myoql.db.CityCodeName
import org.junit.jupiter.api.Test

class TestKtExt_Json : TestBase() {


    @Test
    fun test_FromJson() {
        usingScope(JsonSceneScopeEnum.Web) {
            println("""{"code":123}""".FromJson<CityCodeName>()!!.name)
        }

        usingScope(JsonSceneScopeEnum.Db) {
            println("""{"code":123}""".FromJson<CityCodeName>()!!.name)
        }
    }
}