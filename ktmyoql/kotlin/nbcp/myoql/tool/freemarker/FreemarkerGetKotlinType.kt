package nbcp.myoql.tool.freemarker

import nbcp.base.extend.FindField
import nbcp.base.extend.kotlinTypeName
import nbcp.base.extend.scopes
import java.lang.reflect.Field

// --------私有------
class FreemarkerGetKotlinType : BaseMethodModelFreemarker() {
    override fun exec(list: MutableList<Any?>): Any {
        var entity = getFreemarkerParameter(list[0])
        var paramValue = getFreemarkerParameter(list[1]);

        if (paramValue is String) {
            return entity.javaClass.FindField(paramValue)
                ?.type
                ?.kotlinTypeName
                ?: ""
        }
        throw RuntimeException("不识别的类型${paramValue}: ${paramValue.javaClass.simpleName}")
    }
}