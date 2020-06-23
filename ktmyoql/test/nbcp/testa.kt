package nbcp

import nbcp.comm.AllFields
import nbcp.comm.Define
import nbcp.db.*
import nbcp.db.es.IEsDocument
import nbcp.db.es.tool.generator_mapping
import nbcp.tool.UserCodeGenerator
import org.junit.Test
import java.time.LocalDateTime

@DbEntityGroup("sys")
class e_test(
        var module: String = "", //模块
        var type: String = "",  //类型
        @Define("""{"type":"text","index":"true","boost":"1","analyzer":"ik_max_word","search_analyzer":"ik_max_word"}""")
        var key: String = "",   //实体标志, 查询用： module + key
        @Define("""{"type":"text","index":"true","boost":"1","analyzer":"ik_max_word","search_analyzer":"ik_max_word"}""")
        var msg: String = "",   //消息
        @Define("""{"type":"text","index":"true","boost":"1","analyzer":"ik_max_word","search_analyzer":"ik_max_word"}""")
        var data: String = "",
        @Define("""{"type":"text","index":"true","boost":"1","analyzer":"ik_max_word","search_analyzer":"ik_max_word"}""")
        var remark: String = "",
        var clientIp: String = "",
        var creatAt: LocalDateTime = LocalDateTime.now(),
        var creatorId: String = ""
) : BaseEntity(), IEsDocument

class testa : TestBase() {
    @Test
    fun abc22() {
        var txt = """#{if:name}abc#{elseif:status2}d2#{elseif:status3}d3#{else}!#{endif}""".replace("#", "$")
        var f = db.mor_base.basicUser.entityClass.AllFields.firstOrNull { it.name == "d" };
        var d = UserCodeGenerator.getIfExpression(txt, "if", db.mor_base.basicUser.entityClass::class.java.AllFields,
                f);
        println(d)
    }

    @Test
    fun abc() {
        var m = generator_mapping();
        m.work("mapping", "nbcp", e_test::class.java)
    }
}