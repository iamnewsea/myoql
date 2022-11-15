package nbcp.myoql.tool.freemarker

import nbcp.base.extend.AsString
import nbcp.base.extend.IsType
import nbcp.base.extend.Slice
import java.lang.reflect.Field

class FreemarkerIsType : BaseMethodModelFreemarker() {
    override fun exec(list: MutableList<Any?>): Any {
        var paramValue = getFreemarkerParameter(list[0]);
        var clazzes = getFreemarkerParameterList(*list.Slice(1).toTypedArray()).map { it.AsString() } ;
        if (paramValue is Field) {
            return clazzes.any { paramValue.type.IsType(it) }
        } else if (paramValue is Class<*>) {
            return clazzes.any { paramValue.IsType(it) }
        }
        throw RuntimeException("不识别的类型${paramValue}: ${paramValue.javaClass.simpleName}")
    }
}