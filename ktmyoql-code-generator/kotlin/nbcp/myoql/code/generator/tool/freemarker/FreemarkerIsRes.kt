package nbcp.myoql.code.generator.tool.freemarker

import java.lang.reflect.Field

class FreemarkerIsRes : BaseMethodModelFreemarker() {
    override fun exec(list: MutableList<Any?>): Any {
        var paramValue = getFreemarkerParameter(list[0]);
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