package nbcp.myoql.db.mongo.component


import nbcp.base.comm.*;
import nbcp.base.db.*;
import nbcp.base.enums.*;
import nbcp.base.extend.*;
import nbcp.base.utils.*;
import nbcp.myoql.db.enums.*
import nbcp.myoql.db.*;
import org.slf4j.LoggerFactory
import nbcp.myoql.db.*;
import nbcp.myoql.db.comm.BaseMetaData
import nbcp.myoql.db.mongo.MongoBaseInsertClip
import nbcp.myoql.db.mongo.base.MongoColumnName
import nbcp.myoql.db.mongo.query
import org.springframework.data.mongodb.core.MongoTemplate
import java.lang.RuntimeException

typealias mongoQuery = org.springframework.data.mongodb.core.query.Query

/**
 * mongo 元数据实体的基类
 */
abstract class MongoBaseMetaCollection<T : Any> @JvmOverloads constructor(
    /**
     * 实体的类型
     */
    entityClass: Class<T>,
    tableName: String = "",
    databaseId: String = ""
) : BaseMetaData<T>(entityClass, tableName, databaseId) {
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
        val query = this.query();
        val settingResult = db.mongo.mongoEvents.onQuering(query);
        return query.getMongoTemplate(settingResult.lastOrNull { it.result.dataSource.HasValue }?.result?.dataSource)
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
}


