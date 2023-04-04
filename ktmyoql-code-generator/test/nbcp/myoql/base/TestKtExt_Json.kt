package nbcp.myoql.base

import nbcp.base.enums.JsonSceneScopeEnum
import nbcp.base.extend.FromJson
import nbcp.base.extend.usingScope
import nbcp.myoql.TestBase
import nbcp.myoql.code.generator.compareDbName
import nbcp.myoql.db.CityCodeName
import org.junit.jupiter.api.Test

class TestKtExt_Json : TestBase() {

    @Test
    fun test_FromJson() {
        usingScope(JsonSceneScopeEnum.WEB) {
            println("""{"code":123}""".FromJson<CityCodeName>()!!.name)
        }

        usingScope(JsonSceneScopeEnum.DB) {
            println("""{"code":123}""".FromJson<CityCodeName>()!!.name)
        }
    }
    @Test
    fun test_sort() {
        var list = mutableListOf<String>("a", "order","id","order","create_by")

        list.sortWith { a,b-> compareDbName(a,b) }

        println(list.joinToString())
    }
}