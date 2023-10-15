package nbcp.myoql.code.generator.tool.freemarker

import nbcp.base.extend.*
import java.lang.reflect.Field
import java.lang.reflect.ParameterizedType

class FreemarkerFieldInputTable : BaseMethodModelFreemarker() {
    override fun exec(list: MutableList<Any?>): Any? {
        var paramValue = getFreemarkerParameter(list[0]);

        if (paramValue is Field) {

            if (paramValue.IsCollectionType()) {
                var comType = (paramValue.genericType as ParameterizedType).GetActualClass(0)

                if( comType.IsSimpleType() ){
                    return null;
                }

                return comType;
            }


            if( paramValue.IsArrayType()){
                var comType = paramValue.type.componentType;

                if( comType.IsSimpleType() ){
                    return null;
                }

                return comType;
            }

            return false;
        }
        throw RuntimeException("不识别的类型${paramValue}: ${paramValue.javaClass.simpleName}")
    }
}