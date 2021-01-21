package nbcp.tool

import nbcp.comm.GetActualClass
import nbcp.comm.IsCollectionType
import nbcp.db.Cn
import java.lang.reflect.Field
import java.lang.reflect.ParameterizedType

object CodeGeneratorHelper {
    fun getEntityCommentOnly(entType: Class<*>): String {
        var cn = entType.getAnnotation(Cn::class.java)?.value ?: "";
        if (cn.isNullOrEmpty()) return "";

        return """/**
* ${cn}
*/
"""
    }

    /**
     * 获取表的中文注释及Cn注解
     */
    fun getEntityComment(entType: Class<*>): String {
        var cn = entType.getAnnotation(Cn::class.java)?.value ?: "";
        if (cn.isNullOrEmpty()) return "";

        return """/**
* ${cn}
*/
@Cn("${cn}")
"""
    }

    fun getFieldComment(field: Field): String {
        var cn = field.getAnnotation(Cn::class.java)?.value ?: "";
        if (cn.isNullOrEmpty()) return "";
        return """/**
* ${cn}
*/
@Cn("${cn}")
"""
    }


    fun getEntityCommentValue(entType: Class<*>): String {
        var cn = entType.getAnnotation(Cn::class.java)?.value ?: "";
        if (cn.isNullOrEmpty()) return "";

        return cn;
    }

    fun getFieldCommentValue(field: Field): String {
        var cn = field.getAnnotation(Cn::class.java)?.value ?: "";
        if (cn.isNullOrEmpty()) return "";
        return cn;
    }

    inline fun <reified T> IsListType(field: Field): Boolean {
        var clazz = T::class.java;
        return (field.type.IsCollectionType() && clazz.isAssignableFrom((field.genericType as ParameterizedType).GetActualClass(0))) ||
                (field.type.isArray && clazz.isAssignableFrom(field.type.componentType.javaClass))

    }

    /**
     * 是否是 List枚举
     */
    fun IsListEnum(field: Field): Boolean {
        return (field.type.IsCollectionType() && (field.genericType as ParameterizedType).GetActualClass(0).isEnum) ||
                (field.type.isArray && field.type.componentType.javaClass.isEnum)
    }
}