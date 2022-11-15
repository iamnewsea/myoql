package nbcp.myoql.tool.freemarker

import freemarker.template.TemplateMethodModelEx
import nbcp.myoql.tool.CodeGeneratorHelper
import java.lang.reflect.Field

/**
 *
 */
class Freemarker_Field_IsEnumList : BaseMethodModelFreemarker() {
    override fun exec(p0: MutableList<Any?>): Any {
        var paramValue = getFreemarkerParameter(p0[0]);
        if (paramValue is Field) {
            return CodeGeneratorHelper.IsListEnum(paramValue)
        }
        throw RuntimeException("不识别的类型${paramValue}: ${paramValue.javaClass.simpleName}")
    }
}