package nbcp.myoql.tool.freemarker

import freemarker.template.TemplateMethodModelEx
import java.lang.reflect.Field

class Freemarker_IsRes : BaseMethodModelFreemarker() {
    override fun exec(p0: MutableList<Any?>): Any {
        var paramValue = getFreemarkerParameter(p0[0]);
        if (paramValue is Field) {
            return paramValue.type.isEnum ||
                    paramValue.type == Boolean::class.java
        } else if (paramValue is Class<*>) {
            return paramValue.isEnum ||
                    paramValue == Boolean::class.java
        }
        throw RuntimeException("不识别的类型${paramValue}: ${paramValue.javaClass.simpleName}")
    }
}