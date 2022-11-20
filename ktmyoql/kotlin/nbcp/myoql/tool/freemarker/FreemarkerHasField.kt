package nbcp.myoql.tool.freemarker

import nbcp.base.extend.FindField
import nbcp.base.extend.scopes
import java.lang.reflect.Field

/**
 * 当前上下文实体是否有指定的字段。
 */
class FreemarkerHasField : BaseMethodModelFreemarker() {
    override fun exec(list: MutableList<Any?>): Any {
        val entity = getFreemarkerParameter(list[0])

        var paramValue = getFreemarkerParameter(list[1])
        if (paramValue is String) {
            return entity.javaClass.FindField(paramValue) != null
        }

        throw RuntimeException("不识别的类型${paramValue}: ${paramValue.javaClass.simpleName}")
    }
}