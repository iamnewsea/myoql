package nbcp.myoql.tool.freemarker

import nbcp.base.extend.AsString
import nbcp.base.utils.MyUtil

class FreemarkerFieldValue : BaseMethodModelFreemarker() {
    override fun exec(p0: MutableList<Any?>): Any {
        var paramObj = getFreemarkerParameter(p0[0]);
        var paramName = getFreemarkerParameter(p0[1]).AsString();


        return MyUtil.getValueByWbsPath(paramObj, paramName) ?: ""
    }
}