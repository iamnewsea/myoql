package nbcp.myoql.tool.freemarker

import freemarker.template.TemplateMethodModelEx
import nbcp.base.extend.IsIn
import nbcp.base.extend.Skip

class Freemarker_IsIn : BaseMethodModelFreemarker() {
    override fun exec(p0: MutableList<Any?>): Any {
        var paramValue = getFreemarkerParameter(p0[0]);
        var list = p0.Skip(1).map { getFreemarkerParameter(it) }

        return paramValue.IsIn(*list.toTypedArray())
    }
}