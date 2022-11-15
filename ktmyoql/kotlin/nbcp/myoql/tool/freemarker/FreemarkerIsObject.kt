package nbcp.myoql.tool.freemarker

import nbcp.base.extend.IsCollectionType
import nbcp.base.extend.IsSimpleType
import java.lang.reflect.Field


class FreemarkerIsObject : BaseMethodModelFreemarker() {
    override fun exec(list: MutableList<Any?>): Any {
        var paramValue = getFreemarkerParameter(list[0])
        var type: Class<*>? = null;

        if (paramValue is Field) {
            return isObject(paramValue.type)
        } else if (paramValue is Class<*>) {
            return isObject(paramValue);
        }
        throw RuntimeException("不识别的类型${paramValue}: ${paramValue.javaClass.simpleName}")
    }

    private fun isObject(type: Class<*>): Boolean {
        if (type.isArray) return false;
        if (type.IsCollectionType) return false;
        if (type.IsSimpleType()) return false;
        return true
    }


}



