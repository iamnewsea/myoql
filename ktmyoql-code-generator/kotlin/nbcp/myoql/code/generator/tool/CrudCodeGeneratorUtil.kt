package nbcp.myoql.code.generator.tool

import nbcp.base.comm.JsonMap
import nbcp.base.extend.AllFields
import nbcp.base.extend.AsString
import nbcp.myoql.db.comm.BaseMetaData
import nbcp.myoql.db.es.component.EsBaseMetaEntity
import nbcp.myoql.db.mongo.component.MongoBaseMetaCollection
import nbcp.myoql.db.sql.base.SqlBaseMetaTable
import nbcp.myoql.code.generator.tool.freemarker.*

object CrudCodeGeneratorUtil {
    /**
     * 生成基础的CRUD接口
     */
    @JvmStatic
    fun genMongoMvcCrud(group: String, pkg: String, entity: BaseMetaData<out Any>): String {
        return gen(group, entity, "/myoql-template/mongo/kotlin_mvc_mongo_template_crud.ftl").replace("@pkg@", pkg);
    }

    @JvmStatic
    fun genMySqlMvcCrud(group: String, pkg: String, entity: BaseMetaData<out Any>): String {
        return gen(group, entity, "/myoql-template/mysql/kotlin_mvc_mysql_template_crud.ftl").replace("@pkg@", pkg);
    }

    @JvmStatic
    fun genEsMvcCrud(group: String, pkg: String, entity: BaseMetaData<out Any>): String {
        return gen(group, entity, "/myoql-template/es/kotlin_mvc_es_template_crud.ftl").replace("@pkg@", pkg);
    }

    /**
     * 生成Vue列表页面
     */
    @JvmStatic
    fun genVueList(group: String, entity: BaseMetaData<out Any>): String {
        return gen(group, entity, "/vue-template/vue_list_template.ftl");
    }

    /**
     * 生成Vue卡片页面
     */
    @JvmStatic
    fun genVueCard(group: String, entity: BaseMetaData<out Any>): String {
        return gen(group, entity, "/vue-template/vue_card_template.ftl");
    }

    /**
     * 生成Vue引用
     */
    @JvmStatic
    fun genVueRef(group: String, entity: BaseMetaData<out Any>): String {
        return gen(group, entity, "/vue-template/vue_ref_template.ftl");
    }

    @JvmStatic
    fun genVueCard(entityClass: Class<*>): String {
        return proc(
            "/vue-template/vue_card_prop_template.ftl",
            CrudCodeTemplateData(
                "",
                entityClass,
                "",
                ""
            )
        )
    }


    @JvmStatic
    fun proc(fileName: String, jsonValue: CrudCodeTemplateData): String {
        return FreemarkerUtil.process(fileName, jsonValue)
    }

    private fun gen(group: String, metaEntity: BaseMetaData<out Any>, fileName: String): String {

        var idKey = ""
        var entityClass = metaEntity.entityClass

        if (metaEntity is MongoBaseMetaCollection<*>) {
            idKey = "id"
        } else if (metaEntity is SqlBaseMetaTable<*>) {
            idKey = metaEntity.getAutoIncrementKey().AsString {metaEntity.getUks().firstOrNull { it.size == 1 }?.firstOrNull().AsString("id") }
        } else if (metaEntity is EsBaseMetaEntity<*>) {
            idKey = "id"
        }

        return proc(
            fileName,
            CrudCodeTemplateData(
                group,
                entityClass,
                metaEntity.tableName,
                idKey
            )
        )
    }
}