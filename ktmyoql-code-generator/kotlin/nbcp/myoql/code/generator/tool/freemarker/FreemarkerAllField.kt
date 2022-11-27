package nbcp.myoql.code.generator.tool.freemarker

import nbcp.base.extend.AllFields

class FreemarkerAllField : BaseMethodModelFreemarker() {
    override fun exec(list: MutableList<Any?>): Any {
        var paramValue = getFreemarkerParameter(list[0]);
        if (paramValue is Class<*>) {
            return paramValue.AllFields
        }
        throw RuntimeException("不识别的类型${paramValue}: ${paramValue.javaClass.simpleName}")
    }
}