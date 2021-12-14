package nbcp.tool

import freemarker.cache.ClassTemplateLoader
import nbcp.comm.*
import nbcp.db.Cn
import nbcp.db.DbEntityIndex
import nbcp.db.IdUrl
import nbcp.utils.MyUtil
import java.lang.RuntimeException
import java.lang.reflect.Field
import java.lang.reflect.ParameterizedType
import java.time.LocalDateTime

object CodeGeneratorHelper {
    fun getEntityCommentOnly(entType: Class<*>, remark: String = ""): String {
        var cn = entType.getAnnotation(Cn::class.java)?.value ?: "";
        if (cn.isNullOrEmpty()) return "";

        return """/**
 * ${cn}${remark}
 */"""
    }

    /**
     * 获取表的中文注释及Cn注解
     */
    fun getEntityComment(entType: Class<*>, remark: String = ""): String {
        var cn = entType.getAnnotation(Cn::class.java)?.value ?: "";
        if (cn.isNullOrEmpty()) return "";

        return """/**
 * ${cn}${remark}
 */
@Cn("${cn}")"""
    }

    fun getFieldComment(field: Field): String {
        var cn = field.getAnnotation(Cn::class.java)?.value ?: "";
        if (cn.isNullOrEmpty()) return "";
        return """/**
 * ${cn}
 */
@Cn("${cn}")"""
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
            if( it.unique) {
                uks.add(it.value.joinToString(","))
            }
        }

        if (entType.superclass != null && !procedClasses.contains(entType.superclass.name)) {
            uks.addAll(getEntityUniqueIndexesDefine(entType.superclass, procedClasses))
        }
        return uks;
    }
}