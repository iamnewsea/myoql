package nbcp.myoql.tool.freemarker

import nbcp.base.extend.AsString
import nbcp.base.extend.IsType
import nbcp.base.extend.Slice
import java.lang.reflect.Field

class Freemarker_IsType : BaseMethodModelFreemarker() {
    override fun exec(p0: MutableList<Any?>): Any {
        var paramValue = getFreemarkerParameter(p0[0]);
        var clazzes = getFreemarkerParameterList(*p0.Slice(1).toTypedArray()).map { it.AsString() } ;
        if (paramValue is Field) {
            return clazzes.any { paramValue.type.IsType(it) }
        } else if (paramValue is Class<*>) {
            return clazzes.any { paramValue.IsType(it) }
        }
        throw RuntimeException("不识别的类型${paramValue}: ${paramValue.javaClass.simpleName}")
    }
}