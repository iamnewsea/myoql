package nbcp.db.mongo


import nbcp.comm.AsString
import nbcp.comm.FindField
import org.springframework.data.mongodb.core.query.Criteria
import org.slf4j.LoggerFactory
import nbcp.db.*;
import java.lang.RuntimeException

typealias mongoQuery = org.springframework.data.mongodb.core.query.Query

/**
 * mongo 元数据实体的基类
 */
abstract class MongoBaseMetaCollection<T : IMongoDocument>(val entityClass: Class<T>, entityName: String) :
    BaseMetaData(entityName) {
    //    abstract fun getColumns(): Array<String>;
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }


    /**
     * 插入单条实体
     */
    fun doInsert(entity: T): String {
        var batchInsert = MongoBaseInsertClip(this.tableName)
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


    fun getMongoCriteria(vararg where: Criteria): Criteria {
        if (where.size == 0) return Criteria();
        if (where.size == 1) return where[0];
        return where.first().andOperator(*where.slice(1 until where.size).toTypedArray());
    }
}


