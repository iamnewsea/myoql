package nbcp.base.utils

import nbcp.base.TestBase
import nbcp.base.comm.JsonMap
import nbcp.base.enums.RecursionReturnEnum
import org.junit.jupiter.api.Test

class RecursionUtilTest : TestBase() {
    @Test
    fun test1() {

        var item1 = JsonMap();
        item1["id"] = "1";
        item1["name"] = "a";
        item1["items"] = mutableListOf<JsonMap>(
            JsonMap(
                "id" to "1.1", "items" to mutableListOf<JsonMap>(
                    JsonMap("id" to "1.1.1", "items" to mutableListOf<JsonMap>()),
                    JsonMap("id" to "1.1.2", "items" to mutableListOf<JsonMap>()),
                )
            )
        )

        var item2 = JsonMap();
        item2["id"] = "2";
        item1["name"] = "b";
        item2["items"] = mutableListOf<JsonMap>(
            JsonMap(
                "id" to "2.1", "items" to mutableListOf<JsonMap>(
                    JsonMap("id" to "2.1.1", "items" to mutableListOf<JsonMap>()),
                    JsonMap("id" to "2.1.2", "items" to mutableListOf<JsonMap>()),
                )
            )
        )

        var wbs = RecursionUtil.getWbs(mutableListOf(item1, item2),
            { it.get("items") as MutableList<JsonMap> }, {
                it.get("id") == "2.1"
            })
        println(wbs.map { it.get("id") }.joinToString(","))



        RecursionUtil.execute(
            listOf(item1, item2),
            { it.get("items") as MutableList<JsonMap> }, { pwbs, index ->
                println(pwbs)
                return@execute RecursionReturnEnum.Go
            })
    }

}