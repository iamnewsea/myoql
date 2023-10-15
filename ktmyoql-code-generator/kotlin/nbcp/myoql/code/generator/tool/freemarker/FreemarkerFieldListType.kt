package nbcp.myoql.code.generator.tool.freemarker

import nbcp.base.extend.GetActualClass
import nbcp.base.extend.IsCollectionType
import java.lang.reflect.Field
import java.lang.reflect.ParameterizedType

class FreemarkerFieldListType : BaseMethodModelFreemarker() {
    override fun exec(list: MutableList<Any?>): Any {
        var paramValue = getFreemarkerParameter(list[0]);
        if (paramValue is Field) {
            if (paramValue.type.isArray) {
                return paramValue.type.componentType
            }

            if (paramValue.IsCollectionType()) {
                return (paramValue.genericType as ParameterizedType).GetActualClass(0).simpleName
            }
        }
        throw RuntimeException("不识别的类型${paramValue}: ${paramValue.javaClass.simpleName}")
    }
}