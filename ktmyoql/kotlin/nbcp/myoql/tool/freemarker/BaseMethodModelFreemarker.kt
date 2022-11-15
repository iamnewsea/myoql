package nbcp.myoql.tool.freemarker

import freemarker.ext.beans.StringModel
import freemarker.template.SimpleScalar
import freemarker.template.TemplateMethodModelEx

abstract class BaseMethodModelFreemarker : TemplateMethodModelEx {
    fun getFreemarkerParameterList(vararg p1: Any?): List<Any?> {
        return p1.map { getFreemarkerParameter(it) }
    }


    fun getFreemarkerParameter(p1: Any?): Any {
        if (p1 == null) {
            throw RuntimeException("参数不能为空")
        }

        var paramValue: Any? = p1
        if (p1 is StringModel) {
            paramValue = p1.wrappedObject;
        } else if (p1 is SimpleScalar) {
            paramValue = p1.asString
        }

        if (paramValue == null) {
            throw RuntimeException("参数不能为null")
        }
        return paramValue
    }
}