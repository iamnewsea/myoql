package nbcp.myoql.tool.freemarker

import nbcp.base.extend.IsIn
import nbcp.base.extend.Skip

class FreemarkerIsIn : BaseMethodModelFreemarker() {
    override fun exec(list: MutableList<Any?>): Any {
        var paramValue = getFreemarkerParameter(list[0]);
        var list = list.Skip(1).map { getFreemarkerParameter(it) }

        return paramValue.IsIn(*list.toTypedArray())
    }
}