package nbcp.myoql.code.generator.tool.freemarker

import nbcp.base.extend.HasValue

class FreemarkerHasValue : BaseMethodModelFreemarker() {
    override fun exec(list: MutableList<Any?>): Any {
        if (list.any() == false) return false;

        var paramValue = getFreemarkerParameter(list.get(0));

        if (paramValue is String) {
            return paramValue.HasValue
        }

        return true;
    }
}