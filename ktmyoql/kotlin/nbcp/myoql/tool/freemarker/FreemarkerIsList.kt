package nbcp.myoql.tool.freemarker

import nbcp.myoql.tool.CodeGeneratorHelper
import java.lang.reflect.Field

class FreemarkerIsList : BaseMethodModelFreemarker() {
    override fun exec(p0: MutableList<Any?>): Any {
        var paramValue = getFreemarkerParameter(p0[0])
        var clazz = getFreemarkerParameter(p0[1]) as String;
        if (paramValue is Field) {
            return CodeGeneratorHelper.IsListType(paramValue, clazz)
        }
        throw RuntimeException("不识别的类型${paramValue}: ${paramValue.javaClass.simpleName}")
    }
}