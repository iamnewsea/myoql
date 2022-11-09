package nbcp.base

import nbcp.base.comm.*
import nbcp.base.comm.*;
import nbcp.base.db.*;
import nbcp.base.enums.*;
import nbcp.base.extend.*;
import nbcp.base.utils.*;
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