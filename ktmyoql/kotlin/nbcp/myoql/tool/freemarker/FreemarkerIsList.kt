package nbcp.myoql.tool.freemarker

import nbcp.myoql.tool.CodeGeneratorHelper
import java.lang.reflect.Field

class FreemarkerIsList : BaseMethodModelFreemarker() {
    override fun exec(list: MutableList<Any?>): Any {
        var paramValue = getFreemarkerParameter(list[0])
        var clazz = getFreemarkerParameter(list[1]) as String;
        if (paramValue is Field) {
            return CodeGeneratorHelper.IsListType(paramValue, clazz)
        }
        throw RuntimeException("不识别的类型${paramValue}: ${paramValue.javaClass.simpleName}")
    }
}