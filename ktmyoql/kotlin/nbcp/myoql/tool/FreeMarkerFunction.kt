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

fun _get_value_items(vararg p1: Any?): List<Any?> {
    return p1.map { _get_value_item(it) }
}


fun _get_value_item(p1: Any?): Any {
    if (p1 == null) {
        throw RuntimeException("参数不能为空")
    }

    var paramValue: Any? = p1
    if (p1 is StringModel) {
        paramValue = p1.wrappedObject;
    } else if (p1 is SimpleScalar) {
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

class Freemarker_Field_Cn : TemplateMethodModelEx {
    override fun exec(p0: MutableList<Any?>): Any {
        var paramValue = _get_value_item(p0[0]);

        if (paramValue is Field) {
            return CodeGeneratorHelper.getFieldCommentValue(paramValue).AsString(paramValue.name)
        }
        throw RuntimeException("不识别的类型${paramValue}: ${paramValue.javaClass.simpleName}")
    }
}

class Freemarker_Field_Value : TemplateMethodModelEx {
    override fun exec(p0: MutableList<Any?>): Any {
        var paramObj = _get_value_item(p0[0]);
        var paramName = _get_value_item(p0[1]).AsString();


        return MyUtil.getValueByWbsPath(paramObj, paramName) ?: ""
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

class Freemarker_All_Field : TemplateMethodModelEx {
    override fun exec(p0: MutableList<Any?>): Any {
        var paramValue = _get_value_item(p0[0]);
        if (paramValue is Class<*>) {
            return paramValue.AllFields
        }
        throw RuntimeException("不识别的类型${paramValue}: ${paramValue.javaClass.simpleName}")
    }
}

/**
 *
 */
class Freemarker_Field_IsEnumList : TemplateMethodModelEx {
    override fun exec(p0: MutableList<Any?>): Any {
        var paramValue = _get_value_item(p0[0]);
        if (paramValue is Field) {
            return CodeGeneratorHelper.IsListEnum(paramValue)
        }
        throw RuntimeException("不识别的类型${paramValue}: ${paramValue.javaClass.simpleName}")
    }
}

class Freemarker_Field_ListType : TemplateMethodModelEx {
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
        } else if (paramValue is Class<*>) {
            return paramValue.isEnum ||
                    paramValue == Boolean::class.java
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
        var clazzes = _get_value_items(*p0.Slice(1).toTypedArray()).map { it.AsString() } ;
        if (paramValue is Field) {
            return clazzes.any { paramValue.type.IsType(it) }
        } else if (paramValue is Class<*>) {
            return clazzes.any { paramValue.IsType(it) }
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
        } else if (paramValue is Class<*>) {
            if (paramValue.isArray) return false;
            if (paramValue.IsCollectionType) return false;
            if (paramValue.IsSimpleType()) return false;
            return true
        }
        throw RuntimeException("不识别的类型${paramValue}: ${paramValue.javaClass.simpleName}")
    }
}



