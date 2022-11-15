package nbcp.myoql.tool.freemarker

import nbcp.base.extend.AsString
import nbcp.myoql.tool.CodeGeneratorHelper
import java.lang.reflect.Field

class Freemarker_Field_Cn : BaseMethodModelFreemarker() {
    override fun exec(p0: MutableList<Any?>): Any {
        var paramValue = getFreemarkerParameter(p0[0]);

        if (paramValue is Field) {
            return CodeGeneratorHelper.getFieldCommentValue(paramValue).AsString(paramValue.name)
        }
        throw RuntimeException("不识别的类型${paramValue}: ${paramValue.javaClass.simpleName}")
    }
}