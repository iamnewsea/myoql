package nbcp.myoql.tool.freemarker

import nbcp.base.extend.GetActualClass
import java.lang.reflect.Field
import java.lang.reflect.ParameterizedType

class Freemarker_Field_ListType : BaseMethodModelFreemarker() {
    override fun exec(p0: MutableList<Any?>): Any {
        var paramValue = getFreemarkerParameter(p0[0]);
        if (paramValue is Field) {
            return (paramValue.genericType as ParameterizedType).GetActualClass(0).simpleName
        }
        throw RuntimeException("不识别的类型${paramValue}: ${paramValue.javaClass.simpleName}")
    }
}