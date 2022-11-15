package nbcp.myoql.tool.freemarker

import nbcp.base.extend.kotlinTypeName
import nbcp.base.extend.scopes
import nbcp.base.scope.ContextMapScope
import java.lang.reflect.Field

// --------私有------
class Freemarker_GetKotlinType : BaseMethodModelFreemarker() {
    override fun exec(p0: MutableList<Any?>): Any {
        var paramValue = getFreemarkerParameter(p0[0])
        if (paramValue is String) {
            return (scopes.getLatest<ContextMapScope>()!!.value
                .get("fields") as List<Field>)
                .first { it.name == paramValue }
                .type
                .kotlinTypeName
        }
        throw RuntimeException("不识别的类型${paramValue}: ${paramValue.javaClass.simpleName}")
    }
}