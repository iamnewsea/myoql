package nbcp.myoql.es


import nbcp.myoql.TestBase
import nbcp.base.db.DbEntityGroup
import nbcp.base.db.IdName
import nbcp.myoql.db.*
import nbcp.myoql.db.comm.DbDefine
import nbcp.myoql.db.es.tool.generator_mapping
import org.junit.jupiter.api.Test

@DbEntityGroup("sys")

@DbDefine(
        "key",
        """{"type":"text","index":"true","boost":"1","analyzer":"ik_max_word","search_analyzer":"ik_max_word"}"""
)
@DbDefine(
        "msg",
        """{"type":"text","index":"true","boost":"1","analyzer":"ik_max_word","search_analyzer":"ik_max_word"}"""
)
@DbDefine(
        "data.name",
        """{"type":"text","index":"true","boost":"1","analyzer":"ik_max_word","search_analyzer":"ik_max_word"}"""
)
class e_test(
    var module: String = "", //模块
    var type: String = "",  //类型
    var key: String = "",   //实体标志, 查询用： module + key
    var msg: String = "",   //消息
    var data: IdName = IdName(),
) : BaseEntity()

class testa : TestBase() {
//    @Test
//    fun abc22() {
//        var txt = """#{if:name}abc#{elseif:status2}d2#{elseif:status3}d3#{else}!#{endif}""".replace("#", "$")
//        var f = db.mor_base.basicUser.entityClass.AllFields.firstOrNull { it.name == "d" };
//        var d = MyTemplateProc.getIfExpression(txt, "if", db.mor_base.basicUser.entityClass::class.java.AllFields,
//                f);
//        println(d)
//    }

    @Test
    fun abc() {
        var m = generator_mapping();
        m.work("mapping", "nbcp.myoql.es")
    }
}