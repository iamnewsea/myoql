package nbcp.myoql.tool.freemarker

import nbcp.base.extend.IsArrayType
import nbcp.base.extend.IsCollectionType
import java.lang.reflect.Field

class FreemarkerIsList : BaseMethodModelFreemarker() {
    override fun exec(list: MutableList<Any?>): Any {
        var paramValue = getFreemarkerParameter(list[0])
        var type = getFreemarkerParameter(list[1]) as String;
        if (paramValue is Field) {
            return paramValue.IsCollectionType(type) || paramValue.IsArrayType(type);
        }
        throw RuntimeException("不识别的类型${paramValue}: ${paramValue.javaClass.simpleName}")
    }
}