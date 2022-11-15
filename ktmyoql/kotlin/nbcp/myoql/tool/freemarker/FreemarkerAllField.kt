package nbcp.myoql.tool.freemarker

import nbcp.base.extend.AllFields

class FreemarkerAllField : BaseMethodModelFreemarker() {
    override fun exec(p0: MutableList<Any?>): Any {
        var paramValue = getFreemarkerParameter(p0[0]);
        if (paramValue is Class<*>) {
            return paramValue.AllFields
        }
        throw RuntimeException("不识别的类型${paramValue}: ${paramValue.javaClass.simpleName}")
    }
}