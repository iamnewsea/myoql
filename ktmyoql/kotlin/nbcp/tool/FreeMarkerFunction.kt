package nbcp.tool

import java.io.IOException
import java.io.StringWriter
import java.lang.Exception
import java.util.*
import freemarker.cache.ClassTemplateLoader
import freemarker.ext.beans.StringModel
import freemarker.template.*
import nbcp.comm.*
import nbcp.utils.MyUtil
import org.slf4j.LoggerFactory
import java.lang.RuntimeException
import java.lang.reflect.Field


fun _get_value(p0: MutableList<Any?>, index: Int = 0): Any {
    var p1 = p0[index];

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

class Freemarker_Cn : TemplateMethodModelEx {
    override fun exec(p0: MutableList<Any?>): Any {
        var paramValue = _get_value(p0);

        if (paramValue is Field) {
            return CodeGeneratorHelper.getFieldCommentValue(paramValue).AsString(paramValue.name)
        }
        throw RuntimeException("不识别的类型${paramValue}: ${paramValue.javaClass.simpleName}")
    }
}

class Freemarker_KebabCase : TemplateMethodModelEx {
    override fun exec(p0: MutableList<Any?>): Any {
        var paramValue = _get_value(p0);

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
        var paramValue = _get_value(p0);

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
        var paramValue = _get_value(p0);

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
        var paramValue = _get_value(p0);
        if (paramValue is Field) {
            return CodeGeneratorHelper.IsListEnum(paramValue)
        }
        throw RuntimeException("不识别的类型${paramValue}: ${paramValue.javaClass.simpleName}")
    }
}
class Freemarker_Has : TemplateMethodModelEx {
    override fun exec(p0: MutableList<Any?>): Any {
        var paramValue = _get_value(p0);
        if (paramValue is String) {
            return (scopes.GetLatest<JsonMap>()!!.get("fields") as List<Field>).any { it.name == paramValue }
        }
        throw RuntimeException("不识别的类型${paramValue}: ${paramValue.javaClass.simpleName}")
    }
}
class Freemarker_GetType : TemplateMethodModelEx {
    override fun exec(p0: MutableList<Any?>): Any {
        var paramValue = _get_value(p0) ;
        if (paramValue is String) {
            return (scopes.GetLatest<JsonMap>()!!.get("fields") as List<Field>).first { it.name == paramValue }.type.kotlinTypeName
        }
        throw RuntimeException("不识别的类型${paramValue}: ${paramValue.javaClass.simpleName}")
    }
}
class Freemarker_IsRes : TemplateMethodModelEx {
    override fun exec(p0: MutableList<Any?>): Any {
        var paramValue = _get_value(p0) ;
        if (paramValue is Field) {
            return paramValue!!.type.isEnum ||
                    paramValue.type == Boolean::class.java
        }
        throw RuntimeException("不识别的类型${paramValue}: ${paramValue.javaClass.simpleName}")
    }
}
class Freemarker_IsType : TemplateMethodModelEx {
    override fun exec(p0: MutableList<Any?>): Any {
        var paramValue = _get_value(p0);
        var clazz = _get_value(p0, 1) as String;
        if (paramValue is Field) {
            return paramValue.type.IsType(clazz)
        }
        throw RuntimeException("不识别的类型${paramValue}: ${paramValue.javaClass.simpleName}")
    }
}

class Freemarker_IsList : TemplateMethodModelEx {
    override fun exec(p0: MutableList<Any?>): Any {
        var paramValue = _get_value(p0);
        var clazz = _get_value(p0, 1) as String;
        if (paramValue is Field) {
            return CodeGeneratorHelper.IsListType(paramValue, clazz)
        }
        throw RuntimeException("不识别的类型${paramValue}: ${paramValue.javaClass.simpleName}")
    }
}