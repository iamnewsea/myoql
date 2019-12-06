package nbcp

import nbcp.base.comm.ApiResult
import nbcp.base.extend.FieldTypeJsonMapper
import nbcp.base.extend.GetSetTypeJsonMapper
import nbcp.base.utils.RecursionUtil
import nbcp.db.IdName
import nbcp.db.IdUrl
import org.junit.Test

class testa : TestBase() {

    class c {
        var id: String = "";
        var isAdmin: Boolean = false;

        fun getName(): String {
            return "ok"
        }
    }

    @Test
    fun abc() {
        var list = ApiResult<List<IdName>>()
        list.data = mutableListOf();
        (list.data!! as MutableList).add(IdName("1", "ok"))

        RecursionUtil.recursionJson(list, { k, v, p ->
            println(k + ":" + (v ?: ""))
            return@recursionJson true;
        });
    }
}