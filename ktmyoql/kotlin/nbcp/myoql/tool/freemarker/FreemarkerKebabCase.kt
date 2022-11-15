package nbcp.myoql.tool.freemarker

import nbcp.base.utils.MyUtil
import java.lang.reflect.Field

class FreemarkerKebabCase : BaseMethodModelFreemarker() {
    override fun exec(list: MutableList<Any?>): Any {
        var paramValue = getFreemarkerParameter(list[0]);

        if (paramValue is Field) {
            return MyUtil.getKebabCase(paramValue.name)
        } else if (paramValue is String) {
            return MyUtil.getKebabCase(paramValue)
        }
        throw RuntimeException("不识别的类型${paramValue}: ${paramValue.javaClass.simpleName}")
    }
}