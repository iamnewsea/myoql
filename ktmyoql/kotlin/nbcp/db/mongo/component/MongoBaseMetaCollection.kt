package nbcp.db.mongo


import org.springframework.data.mongodb.core.query.Criteria
import org.slf4j.LoggerFactory
import nbcp.db.*;

typealias mongoQuery = org.springframework.data.mongodb.core.query.Query

/**
 * mongo 元数据实体的基类
 */
abstract class MongoBaseMetaCollection<T : IMongoDocument>(val entityClass: Class<T>, entityName: String) : BaseMetaData(entityName) {
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
        return entity.id
    }


    fun getMongoCriteria(vararg where: Criteria): Criteria {
        if (where.size == 0) return Criteria();
        if (where.size == 1) return where[0];
        return Criteria().andOperator(*where);
    }
}


