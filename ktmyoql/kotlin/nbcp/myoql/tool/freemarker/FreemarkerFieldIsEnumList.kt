package nbcp.myoql.tool.freemarker

import nbcp.base.extend.*
import nbcp.myoql.tool.CodeGeneratorHelper
import java.lang.reflect.Field

/**
 *
 */
class FreemarkerFieldIsEnumList : BaseMethodModelFreemarker() {
    override fun exec(list: MutableList<Any?>): Any {
        var paramValue = getFreemarkerParameter(list[0]);
        if (paramValue is Field) {
            return paramValue.IsCollectionEnum
        }
        throw RuntimeException("不识别的类型${paramValue}: ${paramValue.javaClass.simpleName}")
    }
}