package nbcp.tool

import nbcp.comm.*
import nbcp.db.BaseMetaData
import nbcp.db.IdUrl
import nbcp.db.es.EsBaseMetaEntity
import nbcp.db.mongo.MongoBaseMetaCollection
import nbcp.db.sql.SqlBaseMetaTable
import nbcp.utils.MyUtil
import java.lang.RuntimeException
import java.lang.reflect.Field
import java.time.LocalDateTime
import java.util.*
import kotlin.reflect.jvm.kotlinProperty

object UserCodeGenerator {
    /**
     * 生成基础的CRUD接口
     */
    fun genMongoMvcCrud(group: String, entity: BaseMetaData): String {
        var stream = this::class.java.getResourceAsStream("/kotlin_mvc_mongo_template_crud.txt")
        var text = stream.readBytes().toString(utf8)

        return gen(group, entity, text);
    }

    fun genMySqlMvcCrud(group: String, entity: BaseMetaData): String {
        var stream = this::class.java.getResourceAsStream("/kotlin_mvc_mysql_template_crud.txt")
        var text = stream.readBytes().toString(utf8)

        return gen(group, entity, text);
    }

    fun genEsMvcCrud(group: String, entity: BaseMetaData): String {
        var stream = this::class.java.getResourceAsStream("/kotlin_mvc_es_template_crud.txt")
        var text = stream.readBytes().toString(utf8)

        return gen(group, entity, text);
    }

    /**
     * 生成Vue列表页面
     */
    fun genVueList(group: String, entity: BaseMetaData): String {
        var stream = this::class.java.getResourceAsStream("/vue_list_template.txt")
        var text = stream.readBytes().toString(utf8)

        return gen(group, entity, text);
    }

    /**
     * 生成Vue卡片页面
     */
    fun genVueCard(group: String, entity: BaseMetaData): String {
        var stream = this::class.java.getResourceAsStream("/vue_card_template.txt")
        var text = stream.readBytes().toString(utf8)

        return gen(group, entity, text);
    }

    /**
     * 生成Vue引用
     */
    fun genVueRef(group: String, entity: BaseMetaData): String {
        var stream = this::class.java.getResourceAsStream("/vue_ref_template.txt")
        var text = stream.readBytes().toString(utf8)

        return gen(group, entity, text);
    }


    private fun gen(group: String, metaEntity: BaseMetaData, text: String): String {
        var text = text;
        var id_name = ""
        lateinit var entityClass: Class<*>

        if (metaEntity is MongoBaseMetaCollection<*>) {
            entityClass = metaEntity.entityClass
            id_name = "id"
        } else if (metaEntity is SqlBaseMetaTable<*>) {
            entityClass = metaEntity.tableClass
            id_name = metaEntity.getAutoIncrementKey().AsString(metaEntity.getUks().first { it.size == 1 }[0])
        } else if (metaEntity is EsBaseMetaEntity<*>) {
            entityClass = metaEntity.entityClass
            id_name = "id"
        }

        var entityFields = entityClass.AllFields.MoveToFirst { it.name == "name" }.MoveToFirst { it.name == "id" }
        //先处理${for:fields}


        var status_enum_class = ""
        var statusField = entityFields.firstOrNull { it.name == "status" }
        if (statusField != null) {
            status_enum_class = statusField.type.kotlinTypeName;
        }

        text = CodeGeneratorHelper.procIf(text,"if", entityFields, null);

        text = CodeGeneratorHelper.procFor( text,entityFields, id_name);

        var title = CodeGeneratorHelper.getEntityCommentValue(entityClass).AsString(metaEntity.tableName);

//        var entity_url = MyUtil.getKebabCase(metaEntity.tableName);
//
//        if (entity_url.startsWith(group + "-")) {
//            entity_url = entity_url.substring((group + "-").length);
//        }

        var url = "/${MyUtil.getKebabCase(group)}/${MyUtil.getKebabCase(entityClass.simpleName)}"
        var mapDefine = StringMap(
            "url" to url,
            "group" to group,
            "entity" to entityClass.simpleName,
            "entityField" to MyUtil.getSmallCamelCase(entityClass.simpleName),
            "title" to title,
            "now" to LocalDateTime.now().AsString(),
            "status_enum_class" to status_enum_class,
            "id_name" to id_name
        )
        return MyUtil.formatTemplateJson(text, mapDefine, { key, value, func, funcParam ->
            if (key == "id_name" && func == "type") {
                return@formatTemplateJson entityFields.first { it.name == id_name }.type.kotlinTypeName
            }
            return@formatTemplateJson null
        })
    }
}