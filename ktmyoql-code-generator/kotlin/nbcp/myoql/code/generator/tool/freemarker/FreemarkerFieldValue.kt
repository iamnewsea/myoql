package nbcp.myoql.code.generator.tool.freemarker

import nbcp.base.extend.AsString
import nbcp.base.utils.MyUtil

class FreemarkerFieldValue : BaseMethodModelFreemarker() {
    override fun exec(list: MutableList<Any?>): Any {
        var paramObj = getFreemarkerParameter(list[0]);
        var paramName = getFreemarkerParameter(list[1]).AsString();


        return MyUtil.getValueByWbsPath(paramObj, paramName) ?: ""
    }
}