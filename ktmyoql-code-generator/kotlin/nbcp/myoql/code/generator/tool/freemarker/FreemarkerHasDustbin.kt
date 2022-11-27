package nbcp.myoql.code.generator.tool.freemarker

import nbcp.base.extend.scopes
import nbcp.myoql.db.comm.RemoveToSysDustbin


/**
 * 实体上是否配置了垃圾箱
 */
class FreemarkerHasDustbin : BaseMethodModelFreemarker() {
    override fun exec(list: MutableList<Any?>): Any {
        val entity = getFreemarkerParameter(list[0])
        return entity.javaClass
            .getAnnotation(RemoveToSysDustbin::class.java) != null
    }
}

