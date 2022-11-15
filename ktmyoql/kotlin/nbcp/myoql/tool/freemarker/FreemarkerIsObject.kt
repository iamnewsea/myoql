package nbcp.myoql.tool.freemarker

import nbcp.base.extend.IsCollectionType
import nbcp.base.extend.IsSimpleType
import java.lang.reflect.Field


class FreemarkerIsObject : BaseMethodModelFreemarker() {
    override fun exec(list: MutableList<Any?>): Any {
        var paramValue = getFreemarkerParameter(list[0])
        if (paramValue is Field) {
            if (paramValue.type.isArray) return false;
            if (paramValue.type.IsCollectionType) return false;
            if (paramValue.type.IsSimpleType()) return false;
            return true
        } else if (paramValue is Class<*>) {
            if (paramValue.isArray) return false;
            if (paramValue.IsCollectionType) return false;
            if (paramValue.IsSimpleType()) return false;
            return true
        }
        throw RuntimeException("不识别的类型${paramValue}: ${paramValue.javaClass.simpleName}")
    }
}



