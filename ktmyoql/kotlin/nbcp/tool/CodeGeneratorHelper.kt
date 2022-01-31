package nbcp.tool

import com.fasterxml.jackson.module.kotlin.isKotlinClass
import freemarker.cache.ClassTemplateLoader
import nbcp.comm.*
import nbcp.db.Cn
import nbcp.db.DbEntityIndex
import nbcp.db.IdUrl
import nbcp.utils.MyUtil
import java.lang.RuntimeException
import java.lang.reflect.Field
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Proxy
import java.time.LocalDateTime
import kotlin.reflect.KClass

object CodeGeneratorHelper {

    /**
     * 获取表的中文注释及Cn注解
     */
    fun getEntityComment(entType: Class<*>, remark: String = ""): String {
        var cn = entType.getAnnotation(Cn::class.java)?.value ?: "";
        if (cn.isNullOrEmpty()) return "";

        return """/**
 * ${cn}${remark}
 */"""
    }

    fun getFieldComment(field: Field): String {
        var cn = field.getAnnotation(Cn::class.java)?.value ?: "";
        if (cn.isNullOrEmpty()) return "";
        return """/**
 * ${cn}
 */"""
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

    fun IsListType(field: Field, clazz: String): Boolean {
//        var clazz = T::class.java;
        return (
                field.type.IsCollectionType &&
                        (field.genericType as ParameterizedType).GetActualClass(
                                0
                        ).IsType(clazz)

                ) ||
                (field.type.isArray && field.type.componentType.javaClass.IsType(clazz))

    }

    /**
     * 是否是 List枚举
     */
    fun IsListEnum(field: Field): Boolean {
        return (field.type.IsCollectionType && (field.genericType as ParameterizedType).GetActualClass(0).isEnum) ||
                (field.type.isArray && field.type.componentType.javaClass.isEnum)
    }


    class CodeTemplateData(
            var group: String,
            var entityClass: Class<*>,
            var tableName: String,
            var idKey: String
    )

    fun proc(fileName: String, jsonValue: CodeTemplateData): String {
        var entityClass = jsonValue.entityClass
        var group = jsonValue.group
        var idKey = jsonValue.idKey
        var tableName = jsonValue.tableName


        var entityFields = entityClass.AllFields
                .MoveToFirst { it.name == "code" }
                .MoveToFirst { it.name == "name" }
                .MoveToFirst { it.name == "id" }
        //先处理${for:fields}


        var status_enum_class = ""
        var statusField = entityFields.firstOrNull { it.name == "status" }
        if (statusField != null) {
            status_enum_class = statusField.type.kotlinTypeName;
        }

//        var text = CodeGeneratorHelper.procIf(text, "if", entityFields, null);
//
//        text = CodeGeneratorHelper.procFor(text, entityFields, idKey);


        val title = CodeGeneratorHelper.getEntityCommentValue(entityClass).AsString(tableName);

        val url = "/${MyUtil.getKebabCase(group)}/${MyUtil.getKebabCase(entityClass.simpleName)}"
        var mapDefine = JsonMap(
                "url" to url,
                "group" to group,
                "entity_type" to entityClass,
                "entity" to entityClass.simpleName,
                "fields" to entityClass.AllFields,
                "entityField" to MyUtil.getSmallCamelCase(entityClass.simpleName),
                "title" to title,
                "status_enum_class" to status_enum_class,
                "idKey" to idKey,

                "kotlin_type" to Freemarker_GetKotlinType(),
                "has" to Freemarker_Has(),
                "has_dustbin" to Freemarker_HasDustbin()
        )

        return FreemarkerUtil.process(fileName, mapDefine)
    }


    fun getEntityUniqueIndexesDefine(entType: Class<*>, procedClasses: MutableSet<String> = mutableSetOf()): Set<String> {
        procedClasses.add(entType.name)

        val uks = mutableSetOf<String>()

        entType.getAnnotationsByType(DbEntityIndex::class.java).forEach {
            if (it.unique) {
                uks.add(it.value.joinToString(","))
            }
        }

        if (entType.superclass != null && !procedClasses.contains(entType.superclass.name)) {
            uks.addAll(getEntityUniqueIndexesDefine(entType.superclass, procedClasses))
        }
        return uks;
    }

    fun getAnnotations(annotations: Array<out Annotation>): String {
        return annotations
                .map { an ->
                    return@map getAnnotation(an)
                }
                .filter { it.HasValue }
                .map { const.line_break + it }.joinToString("")
    }

    private fun getAnnotation(an: Annotation, isRoot: Boolean = true): String {
        if (an is Metadata) return "";
        if (an is Proxy == false) {
            throw RuntimeException("非 Proxy!")
        }

        var h = Proxy.getInvocationHandler(an);
        var members = MyUtil.getValueByWbsPath(h, "memberValues") as Map<String, Any?>?;
        if (members == null) return "";


        var ret = "";

        if (isRoot) {
            ret += "@"
        }

        ret += an.annotationClass.qualifiedName


        if (members.any() == false) {
            return ret;
        }

        var list = members.map { kv ->
            var key = kv.key
            var v = kv.value!!;

            return@map """${key} = ${getValueString(v)}"""
        }

        return ret + "(" + list.joinToString(", ") + ")"
    }

    private fun getValueString(value: Any): String {
        if (value is Class<*>) {
            return """${value.name}::class"""
        } else if (value is Annotation) {
            return getAnnotation(value, false)
        }

        var v_type = value::class.java;
        if (v_type.IsStringType) {
            return """${"\""}""${value.AsString()}""${"\""}""""
        } else if (v_type.IsNumberType) {
            return value.AsString()
        } else if (v_type.IsBooleanType) {
            return value.AsString().lowercase()
        } else if (v_type.isArray) {
            return "arrayOf(" + (value as Array<Any>).map { getValueString(it) }.joinToString(", ") + ")"
        } else if (v_type.IsCollectionType) {
            return "listOf(" + (value as List<Any>).map { getValueString(it) }.joinToString(", ") + ")"
        } else if (v_type.isAssignableFrom(Map::class.java)) {
            throw RuntimeException("不识别Map")
        }

        var args = v_type.AllFields.map { return@map it.name + " = " + getValueString(it.get(value)) }.joinToString(", ")
        //对象
        return v_type.name + "(" + args + ")"
    }
}