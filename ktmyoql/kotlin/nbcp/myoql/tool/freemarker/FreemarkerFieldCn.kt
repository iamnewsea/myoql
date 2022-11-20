package nbcp.myoql.tool.freemarker

import nbcp.base.extend.AsString
import nbcp.base.utils.CnAnnotationUtil
import java.lang.reflect.Field

class FreemarkerFieldCn : BaseMethodModelFreemarker() {
    override fun exec(list: MutableList<Any?>): Any {
        var paramValue = getFreemarkerParameter(list[0]);

        if (paramValue is Field) {
            return CnAnnotationUtil.getCnValue(paramValue).AsString(paramValue.name)
        }
        throw RuntimeException("不识别的类型${paramValue}: ${paramValue.javaClass.simpleName}")
    }
}