package nbcp.myoql.tool.freemarker

import freemarker.ext.beans.StringModel
import freemarker.template.SimpleScalar
import nbcp.base.extend.HasValue

class FreemarkerHasValue : BaseMethodModelFreemarker() {
    override fun exec(p0: MutableList<Any?>): Any {
        var p1 = p0[0];

        var paramValue: Any? = null
        if (p1 != null) {
            if (p1 is StringModel) {
                paramValue = p1.wrappedObject;
            }

            if (p1 is SimpleScalar) {
                paramValue = p1.asString
            }
        }

        if (paramValue == null) {
            return false;
        }

        if (paramValue is String) {
            return paramValue.HasValue
        }

        return true;
    }
}