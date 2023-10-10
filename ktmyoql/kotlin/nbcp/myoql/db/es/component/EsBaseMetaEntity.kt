package nbcp.myoql.db.es.component


import nbcp.base.extend.AsString
import nbcp.base.extend.FindField
import nbcp.myoql.db.BaseEntity
import nbcp.myoql.db.comm.BaseMetaData

/**
 * es 元数据实体的基类
 */
abstract class EsBaseMetaEntity<T:Any>(
    entityClass: Class<T>,
    tableName: String = "",
    databaseId: String = ""
) : BaseMetaData<T>(entityClass, tableName, databaseId) {
    //    abstract fun getColumns(): Array<String>;
    companion object {
    }

    /**
     * 插入单条实体
     */
    fun doInsert(entity: T): String {
        var batchInsert = EsBaseBulkInsertClip(this.tableName)
        batchInsert.addEntity(entity)
        batchInsert.exec();

        if (entity is BaseEntity) {
            return entity.id
        } else if (entity is Map<*, *>) {
            var idValue = entity.get("_id");
            if (idValue != null) {
                return idValue.AsString()
            }
        }

        val idField = entity.javaClass.FindField("id")
        if (idField != null) {
            return idField.get(entity).AsString();
        }
        throw RuntimeException("找不到 id")
    }
}


