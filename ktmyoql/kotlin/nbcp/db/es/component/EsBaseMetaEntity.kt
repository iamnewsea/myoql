package nbcp.db.es


import nbcp.comm.*
import java.io.Serializable
import nbcp.db.*;
import java.lang.RuntimeException

/**
 * es 元数据实体的基类
 */
abstract class EsBaseMetaEntity<T : Serializable>(val entityClass: Class<T>, entityName: String) : BaseMetaData(entityName) {
    //    abstract fun getColumns(): Array<String>;
    companion object {
    }

    /**
     * 插入单条实体
     */
    fun doInsert(entity: T): String {
        var batchInsert = EsBaseInsertClip(this.tableName)
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

        var idField = entity.javaClass.FindField("id")
        if (idField != null) {
            idField.isAccessible = true;
            return idField.get(entity).AsString();
        }
        throw RuntimeException("找不到 id")
    }
}


