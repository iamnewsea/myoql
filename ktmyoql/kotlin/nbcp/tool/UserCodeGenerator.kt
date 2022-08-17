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
    @JvmStatic
    fun genMongoMvcCrud(group: String, pkg: String, entity: BaseMetaData): String {
        return gen(group, entity, "/myoql-template/kotlin_mvc_mongo_template_crud.ftl").replace("@pkg@", pkg);
    }

    @JvmStatic
    fun genMySqlMvcCrud(group: String, pkg: String, entity: BaseMetaData): String {
        return gen(group, entity, "/myoql-template/kotlin_mvc_mysql_template_crud.ftl").replace("@pkg@", pkg);
    }

    @JvmStatic
    fun genEsMvcCrud(group: String, pkg: String, entity: BaseMetaData): String {
        return gen(group, entity, "/myoql-template/kotlin_mvc_es_template_crud.ftl").replace("@pkg@", pkg);
    }

    /**
     * 生成Vue列表页面
     */
    @JvmStatic
    fun genVueList(group: String, entity: BaseMetaData): String {
        return gen(group, entity, "/myoql-template/vue_list_template.ftl");
    }

    /**
     * 生成Vue卡片页面
     */
    @JvmStatic
    fun genVueCard(group: String, entity: BaseMetaData): String {
        return gen(group, entity, "/myoql-template/vue_card_template.ftl");
    }

    /**
     * 生成Vue引用
     */
    @JvmStatic
    fun genVueRef(group: String, entity: BaseMetaData): String {
        return gen(group, entity, "/myoql-template/vue_ref_template.ftl");
    }

    @JvmStatic
    fun genVueCard(entityClass: Class<*>): String {
        return CodeGeneratorHelper.proc(
            "/myoql-template/vue_card_prop_template.ftl",
            CodeGeneratorHelper.CodeTemplateData(
                "",
                entityClass,
                "",
                ""
            )
        )
    }

    private fun gen(group: String, metaEntity: BaseMetaData, fileName: String): String {

        var idKey = ""
        lateinit var entityClass: Class<*>

        if (metaEntity is MongoBaseMetaCollection<*>) {
            entityClass = metaEntity.entityClass
            idKey = "id"
        } else if (metaEntity is SqlBaseMetaTable<*>) {
            entityClass = metaEntity.tableClass
            idKey = metaEntity.getAutoIncrementKey().AsString(metaEntity.getUks().first { it.size == 1 }[0])
        } else if (metaEntity is EsBaseMetaEntity<*>) {
            entityClass = metaEntity.entityClass
            idKey = "id"
        }

        return CodeGeneratorHelper.proc(
            fileName,
            CodeGeneratorHelper.CodeTemplateData(
                group,
                entityClass,
                metaEntity.tableName,
                idKey
            )
        )
    }
}