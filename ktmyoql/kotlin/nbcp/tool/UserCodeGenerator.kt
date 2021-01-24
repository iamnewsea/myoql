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
        var id_key = ""
        lateinit var entityClass: Class<*>

        if (metaEntity is MongoBaseMetaCollection<*>) {
            entityClass = metaEntity.entityClass
            id_key = "id"
        } else if (metaEntity is SqlBaseMetaTable<*>) {
            entityClass = metaEntity.tableClass
            id_key = metaEntity.getAutoIncrementKey().AsString(metaEntity.getUks().first { it.size == 1 }[0])
        } else if (metaEntity is EsBaseMetaEntity<*>) {
            entityClass = metaEntity.entityClass
            id_key = "id"
        }

        text = CodeGeneratorHelper.proc(
            text,
            CodeGeneratorHelper.CodeTemplateData(
                group,
                entityClass,
                metaEntity.tableName,
                id_key
            )
        )

        return text;
    }
}