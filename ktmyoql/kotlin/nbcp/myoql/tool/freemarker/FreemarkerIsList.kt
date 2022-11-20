package nbcp.myoql.tool.freemarker

import nbcp.base.extend.IsArrayType
import nbcp.base.extend.IsCollectionType
import nbcp.myoql.tool.CodeGeneratorHelper
import java.lang.reflect.Field

class FreemarkerIsList : BaseMethodModelFreemarker() {
    override fun exec(list: MutableList<Any?>): Any {
        var paramValue = getFreemarkerParameter(list[0])
        var clazz = getFreemarkerParameter(list[1]) as String;
        if (paramValue is Field) {
            return paramValue.IsCollectionType(clazz) || paramValue.IsArrayType(clazz);
        }
        throw RuntimeException("不识别的类型${paramValue}: ${paramValue.javaClass.simpleName}")
    }
}