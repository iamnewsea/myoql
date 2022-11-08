package nbcp.myoql.tool

import freemarker.ext.beans.StringModel
import freemarker.template.*
import nbcp.base.comm.*;
import nbcp.base.db.*;
import nbcp.base.enums.*;
import nbcp.base.extend.*;
import nbcp.base.utils.*;
import nbcp.myoql.db.enums.*
import nbcp.myoql.db.*;
import nbcp.myoql.db.comm.*
import nbcp.base.scope.ContextMapScope
import nbcp.base.utils.MyUtil
import java.lang.RuntimeException
import java.lang.reflect.Field
import java.lang.reflect.ParameterizedType



// --------私有------
class Freemarker_GetKotlinType : TemplateMethodModelEx {
    override fun exec(p0: MutableList<Any?>): Any {
        var paramValue = _get_value_item(p0[0])
        if (paramValue is String) {
            return (scopes.getLatest<ContextMapScope>()!!.value
                .get("fields") as List<Field>)
                .first { it.name == paramValue }
                .type
                .kotlinTypeName
        }
        throw RuntimeException("不识别的类型${paramValue}: ${paramValue.javaClass.simpleName}")
    }
}




class Freemarker_Has : TemplateMethodModelEx {
    override fun exec(p0: MutableList<Any?>): Any {
        var paramValue = _get_value_item(p0[0])
        if (paramValue is String) {
            return (scopes.getLatest<ContextMapScope>()!!.value
                .get("fields") as List<Field>)
                .any { it.name == paramValue }
        }
        throw RuntimeException("不识别的类型${paramValue}: ${paramValue.javaClass.simpleName}")
    }
}


/**
 * 实体上是否配置了垃圾箱
 */
class Freemarker_HasDustbin : TemplateMethodModelEx {
    override fun exec(p0: MutableList<Any?>): Any {
        return (scopes.getLatest<ContextMapScope>()!!.value
            .get("entity_type") as Class<*>)
            .getAnnotation(RemoveToSysDustbin::class.java) != null
    }
}

