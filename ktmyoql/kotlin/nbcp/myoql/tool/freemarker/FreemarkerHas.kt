package nbcp.myoql.tool.freemarker

import nbcp.base.extend.scopes
import nbcp.base.scope.ContextMapScope
import java.lang.reflect.Field

/**
 * 当前上下文实体是否有指定的字段。
 */
class FreemarkerHas : BaseMethodModelFreemarker() {
    override fun exec(list: MutableList<Any?>): Any {
        var paramValue = getFreemarkerParameter(list[0])
        if (paramValue is String) {
            return (scopes.getLatest<ContextMapScope>()!!.value
                .get("fields") as List<Field>)
                .any { it.name == paramValue }
        }
        throw RuntimeException("不识别的类型${paramValue}: ${paramValue.javaClass.simpleName}")
    }
}