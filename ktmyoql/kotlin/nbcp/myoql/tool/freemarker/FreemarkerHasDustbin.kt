package nbcp.myoql.tool.freemarker

import nbcp.base.extend.scopes
import nbcp.base.scope.ContextMapScope
import nbcp.myoql.db.comm.RemoveToSysDustbin


/**
 * 实体上是否配置了垃圾箱
 */
class FreemarkerHasDustbin : BaseMethodModelFreemarker() {
    override fun exec(p0: MutableList<Any?>): Any {
        return (scopes.getLatest<ContextMapScope>()!!.value
            .get("entity_type") as Class<*>)
            .getAnnotation(RemoveToSysDustbin::class.java) != null
    }
}

