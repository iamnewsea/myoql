package nbcp.myoql.code.generator.tool.freemarker

import nbcp.base.extend.IsArrayType
import nbcp.base.extend.IsCollectionType
import java.lang.reflect.Field

class FreemarkerIsList : BaseMethodModelFreemarker() {
    override fun exec(list: MutableList<Any?>): Any {
        var paramValue = getFreemarkerParameter(list[0])
        if (paramValue is Field) {
            if (list.size > 1) {
                var type = getFreemarkerParameter(list[1]) as String;
                return paramValue.IsCollectionType(type) || paramValue.IsArrayType(type);
            } else {
                return paramValue.IsCollectionType() || paramValue.IsArrayType()
            }
        }
        throw RuntimeException("不识别的类型${paramValue}: ${paramValue.javaClass.simpleName}")
    }
}