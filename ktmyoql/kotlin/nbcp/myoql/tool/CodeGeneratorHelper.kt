package nbcp.myoql.tool

import nbcp.base.comm.JsonMap
import nbcp.base.db.annotation.*
import nbcp.base.extend.*
import nbcp.base.utils.CnAnnotationUtil
import nbcp.base.utils.MyUtil
import java.lang.reflect.Field
import java.lang.reflect.ParameterizedType
import nbcp.myoql.tool.freemarker.*

object CodeGeneratorHelper {


    @JvmStatic
    fun proc(fileName: String, jsonValue: CrudCodeTemplateData): String {
        var entityClass = jsonValue.entityClass
        var group = jsonValue.group
        var idKey = jsonValue.idKey
        var tableName = jsonValue.tableName


        var entityFields = entityClass.AllFields
            .sortedBy {
                if (it.name == "id") return@sortedBy -9
                if (it.name == "name") return@sortedBy -8
                if (it.name == "code") return@sortedBy -7

                return@sortedBy 0
            }
        //先处理${for:fields}


        var status_enum_class = ""
        var statusField = entityFields.firstOrNull { it.name == "status" }
        if (statusField != null) {
            status_enum_class = statusField.type.kotlinTypeName;
        }

//        var text = CodeGeneratorHelper.procIf(text, "if", entityFields, null);
//
//        text = CodeGeneratorHelper.procFor(text, entityFields, idKey);


        val title = CnAnnotationUtil.getCnValue(entityClass).AsString(tableName);

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

            "kotlin_type" to FreemarkerGetKotlinType(),
            "has" to FreemarkerHas(),
            "has_dustbin" to FreemarkerHasDustbin()
        )

        return FreemarkerUtil.process(fileName, mapDefine)
    }

    /**
     * 查找所有唯一索引，每组用逗号分隔。
     */
    @JvmOverloads
    @JvmStatic
    fun getEntityUniqueIndexesDefine(
        entType: Class<*>,
        procedClasses: MutableSet<String> = mutableSetOf()
    ): Set<String> {
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


}