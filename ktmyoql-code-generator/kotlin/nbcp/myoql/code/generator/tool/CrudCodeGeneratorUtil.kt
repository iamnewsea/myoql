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
        return gen(group, entity, "/myoql-template/mongo/kotlin-mvc-mongo-template-crud.ftl").replace("@pkg@", pkg);
    }

    @JvmStatic
    fun genMySqlMvcCrud(group: String, pkg: String, entity: BaseMetaData<out Any>): String {
        return gen(group, entity, "/myoql-template/mysql/kotlin-mvc-mysql-template-crud.ftl").replace("@pkg@", pkg);
    }

    @JvmStatic
    fun genEsMvcCrud(group: String, pkg: String, entity: BaseMetaData<out Any>): String {
        return gen(group, entity, "/myoql-template/es/kotlin-mvc-es-template-crud.ftl").replace("@pkg@", pkg);
    }

    /**
     * 生成Vue列表页面
     */
    @JvmStatic
    fun genVueList(group: String, entity: BaseMetaData<out Any>): String {
        return gen(group, entity, "/vue-template/vue-list-template.ftl");
    }

    /**
     * 生成Vue卡片页面
     */
    @JvmStatic
    fun genVueCard(group: String, entity: BaseMetaData<out Any>): String {
        return gen(group, entity, "/vue-template/vue-card-template.ftl");
    }

    /**
     * 生成Vue引用
     */
    @JvmStatic
    fun genVueRef(group: String, entity: BaseMetaData<out Any>): String {
        return gen(group, entity, "/vue-template/vue-ref-template.ftl");
    }


    @JvmStatic
    fun genVueRefInputTable(entityClass: Class<*>): String{
        return proc(
            "/vue-template/vue-ref-input-table-template.ftl",
            CrudCodeTemplateData(
                "",
                entityClass,
                "",
                ""
            )
        )
    }

    @JvmStatic
    fun genVueRefCard(entityClass: Class<*>): String {
        return proc(
            "/vue-template/vue-ref-card-template.ftl",
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