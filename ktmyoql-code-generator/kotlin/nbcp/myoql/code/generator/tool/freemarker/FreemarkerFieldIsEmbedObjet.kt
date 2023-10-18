package nbcp.myoql.code.generator.tool.freemarker

import nbcp.base.extend.*
import nbcp.myoql.db.BaseEntity
import java.lang.reflect.Field
import java.lang.reflect.ParameterizedType

class FreemarkerFieldIsEmbedObjet : BaseMethodModelFreemarker() {
    override fun exec(list: MutableList<Any?>): Any {
        var paramValue = getFreemarkerParameter(list[0]);

        if (paramValue is Field) {

            if (paramValue.IsCollectionType()) {
                return false;
            }


            if (paramValue.IsArrayType()) {
                return false;
            }

            if (paramValue.type.IsSimpleType()) {
                return false;
            }

            if (paramValue.type is BaseEntity) {
                return false;
            }
            return true;
        }
        throw RuntimeException("不识别的类型${paramValue}: ${paramValue.javaClass.simpleName}")
    }
}