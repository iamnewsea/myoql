package nbcp.myoql.tool.freemarker

import freemarker.template.TemplateMethodModelEx
import nbcp.base.utils.MyUtil
import java.lang.reflect.Field

class Freemarker_KebabCase : BaseMethodModelFreemarker() {
    override fun exec(p0: MutableList<Any?>): Any {
        var paramValue = getFreemarkerParameter(p0[0]);

        if (paramValue is Field) {
            return MyUtil.getKebabCase(paramValue.name)
        } else if (paramValue is String) {
            return MyUtil.getKebabCase(paramValue)
        }
        throw RuntimeException("不识别的类型${paramValue}: ${paramValue.javaClass.simpleName}")
    }
}