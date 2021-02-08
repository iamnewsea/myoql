package nbcp.tool

import java.io.IOException
import java.io.StringWriter
import java.lang.Exception
import java.util.*
import freemarker.cache.ClassTemplateLoader
import freemarker.ext.beans.StringModel
import freemarker.template.*
import nbcp.comm.*
import nbcp.db.RemoveToSysDustbin
import nbcp.utils.MyUtil
import org.slf4j.LoggerFactory
import java.lang.RuntimeException
import java.lang.reflect.Field
import java.lang.reflect.ParameterizedType


fun _get_value_item(p1: Any?): Any {
    if (p1 == null) {
        throw RuntimeException("参数不能为空")
    }
    var paramValue: Any? = null
    if (p1 is StringModel) {
        paramValue = p1.wrappedObject;
    }

    if (p1 is SimpleScalar) {
        paramValue = p1.asString
    }
    if (paramValue == null) {
        throw RuntimeException("参数不能为null")
    }
    return paramValue
}

class Freemarker_HasValue : TemplateMethodModelEx {
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

class Freemarker_Cn : TemplateMethodModelEx {
    override fun exec(p0: MutableList<Any?>): Any {
        var paramValue = _get_value_item(p0[0]);

        if (paramValue is Field) {
            return CodeGeneratorHelper.getFieldCommentValue(paramValue).AsString(paramValue.name)
        }
        throw RuntimeException("不识别的类型${paramValue}: ${paramValue.javaClass.simpleName}")
    }
}

class Freemarker_KebabCase : TemplateMethodModelEx {
    override fun exec(p0: MutableList<Any?>): Any {
        var paramValue = _get_value_item(p0[0]);

        if (paramValue is Field) {
            return MyUtil.getKebabCase(paramValue.name)
        } else if (paramValue is String) {
            return MyUtil.getKebabCase(paramValue)
        }
        throw RuntimeException("不识别的类型${paramValue}: ${paramValue.javaClass.simpleName}")
    }
}


class Freemarker_BigCamelCase : TemplateMethodModelEx {
    override fun exec(p0: MutableList<Any?>): Any {
        var paramValue = _get_value_item(p0[0]);

        if (paramValue is Field) {
            return MyUtil.getBigCamelCase(paramValue.name)
        } else if (paramValue is String) {
            return MyUtil.getBigCamelCase(paramValue)
        }
        throw RuntimeException("不识别的类型${paramValue}: ${paramValue.javaClass.simpleName}")
    }
}

class Freemarker_SmallCamelCase : TemplateMethodModelEx {
    override fun exec(p0: MutableList<Any?>): Any {
        var paramValue = _get_value_item(p0[0]);

        if (paramValue is Field) {
            return MyUtil.getSmallCamelCase(paramValue.name)
        } else if (paramValue is String) {
            return MyUtil.getSmallCamelCase(paramValue)
        }
        throw RuntimeException("不识别的类型${paramValue}: ${paramValue.javaClass.simpleName}")
    }
}

/**
 *
 */
class Freemarker_IsEnumList : TemplateMethodModelEx {
    override fun exec(p0: MutableList<Any?>): Any {
        var paramValue = _get_value_item(p0[0]);
        if (paramValue is Field) {
            return CodeGeneratorHelper.IsListEnum(paramValue)
        }
        throw RuntimeException("不识别的类型${paramValue}: ${paramValue.javaClass.simpleName}")
    }
}

class Freemarker_ListType : TemplateMethodModelEx {
    override fun exec(p0: MutableList<Any?>): Any {
        var paramValue = _get_value_item(p0[0]);
        if (paramValue is Field) {
            return (paramValue.genericType as ParameterizedType).GetActualClass(0).simpleName
        }
        throw RuntimeException("不识别的类型${paramValue}: ${paramValue.javaClass.simpleName}")
    }
}

class Freemarker_IsRes : TemplateMethodModelEx {
    override fun exec(p0: MutableList<Any?>): Any {
        var paramValue = _get_value_item(p0[0]);
        if (paramValue is Field) {
            return paramValue.type.isEnum ||
                    paramValue.type == Boolean::class.java
        }
        throw RuntimeException("不识别的类型${paramValue}: ${paramValue.javaClass.simpleName}")
    }
}

class Freemarker_IsIn : TemplateMethodModelEx {
    override fun exec(p0: MutableList<Any?>): Any {
        var paramValue = _get_value_item(p0[0]);
        var list = p0.Skip(1).map { _get_value_item(it) }

        return paramValue.IsIn(*list.toTypedArray())
    }
}

class Freemarker_IsType : TemplateMethodModelEx {
    override fun exec(p0: MutableList<Any?>): Any {
        var paramValue = _get_value_item(p0[0]);
        var clazz = _get_value_item(p0[1]) as String;
        if (paramValue is Field) {
            return paramValue.type.IsType(clazz)
        }
        throw RuntimeException("不识别的类型${paramValue}: ${paramValue.javaClass.simpleName}")
    }
}

class Freemarker_IsList : TemplateMethodModelEx {
    override fun exec(p0: MutableList<Any?>): Any {
        var paramValue = _get_value_item(p0[0])
        var clazz = _get_value_item(p0[1]) as String;
        if (paramValue is Field) {
            return CodeGeneratorHelper.IsListType(paramValue, clazz)
        }
        throw RuntimeException("不识别的类型${paramValue}: ${paramValue.javaClass.simpleName}")
    }
}

class Freemarker_IsObject : TemplateMethodModelEx {
    override fun exec(p0: MutableList<Any?>): Any {
        var paramValue = _get_value_item(p0[0])
        if (paramValue is Field) {
            if (paramValue.type.isArray) return false;
            if (paramValue.type.IsCollectionType) return false;
            if (paramValue.type.IsSimpleType()) return false;
            return true
        }
        throw RuntimeException("不识别的类型${paramValue}: ${paramValue.javaClass.simpleName}")
    }
}

// --------私有------
class Freemarker_GetKotlinType : TemplateMethodModelEx {
    override fun exec(p0: MutableList<Any?>): Any {
        var paramValue = _get_value_item(p0[0])
        if (paramValue is String) {
            return (scopes.GetLatest<JsonMap>()!!
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
            return (scopes.GetLatest<JsonMap>()!!
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
        return (scopes.GetLatest<JsonMap>()!!
            .get("entity_type") as Class<*>)
            .getAnnotation(RemoveToSysDustbin::class.java) != null
    }
}

