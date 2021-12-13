package nbcp.db.mongo


import nbcp.comm.AllFields
import nbcp.comm.AsString
import nbcp.comm.FindField
import org.springframework.data.mongodb.core.query.Criteria
import org.slf4j.LoggerFactory
import nbcp.db.*;
import org.springframework.data.mongodb.core.MongoTemplate
import java.lang.RuntimeException
import java.io.Serializable

typealias mongoQuery = org.springframework.data.mongodb.core.query.Query

/**
 * mongo 元数据实体的基类
 */
abstract class MongoBaseMetaCollection<T : Serializable> @JvmOverloads constructor(
    /**
     * 实体的类型
     */
    val entityClass: Class<T>,
    entityName: String,
    databaseId: String = ""
) : BaseMetaData(entityName, databaseId) {
    //    abstract fun getColumns(): Array<String>;
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }

    @Transient
    private var _columns = arrayOf<MongoColumnName>()
    fun getColumns(): Array<MongoColumnName> {
        if (_columns.isNotEmpty()) {
            return _columns;
        }

        _columns = this::class.java.AllFields.filter { MongoColumnName::class.java.isAssignableFrom(it.type) }
            .map { it.get(this) as MongoColumnName }
            .toTypedArray()
        return _columns;
    }

    fun getMongoTemplate(): MongoTemplate {
        return this.query().mongoTemplate;
    }


    /**
     * 插入单条实体
     */
    fun doInsert(entity: T): String {
        return insertAny(entity);
    }

    fun doInsert(entity: Map<*, Any?>): String {
        return insertAny(entity);
    }

    private fun insertAny(entity: Any): String {
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
            return idField.get(entity).AsString();
        }

        throw RuntimeException("找不到 id")
    }


    fun getMongoCriteria(vararg where: Criteria): Criteria {
        if (where.size == 0) return Criteria();
        if (where.size == 1) return where[0];
        return Criteria().andOperator(*where);
    }
}


