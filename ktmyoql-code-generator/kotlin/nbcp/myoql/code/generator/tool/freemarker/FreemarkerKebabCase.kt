package nbcp.myoql.code.generator.tool.freemarker

import nbcp.base.utils.MyUtil
import nbcp.base.utils.StringUtil
import java.lang.reflect.Field

class FreemarkerKebabCase : BaseMethodModelFreemarker() {
    override fun exec(list: MutableList<Any?>): Any {
        var paramValue = getFreemarkerParameter(list[0]);

        if (paramValue is Field) {
            return StringUtil.getKebabCase(paramValue.name)
        } else if (paramValue is String) {
            return StringUtil.getKebabCase(paramValue)
        }
        throw RuntimeException("不识别的类型${paramValue}: ${paramValue.javaClass.simpleName}")
    }
}