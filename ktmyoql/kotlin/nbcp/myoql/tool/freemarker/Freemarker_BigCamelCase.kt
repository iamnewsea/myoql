package nbcp.myoql.tool.freemarker

import nbcp.base.utils.MyUtil
import java.lang.reflect.Field

class Freemarker_BigCamelCase : BaseMethodModelFreemarker() {
    override fun exec(p0: MutableList<Any?>): Any {
        var paramValue = getFreemarkerParameter(p0[0]);

        if (paramValue is Field) {
            return MyUtil.getBigCamelCase(paramValue.name)
        } else if (paramValue is String) {
            return MyUtil.getBigCamelCase(paramValue)
        }
        throw RuntimeException("不识别的类型${paramValue}: ${paramValue.javaClass.simpleName}")
    }
}