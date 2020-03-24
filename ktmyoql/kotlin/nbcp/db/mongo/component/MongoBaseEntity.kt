package nbcp.db.mongo


import org.bson.Document
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import nbcp.base.extend.*
import nbcp.base.utils.SpringUtil
import nbcp.db.db

import nbcp.db.mongo.*
import nbcp.db.mongo.component.MongoBaseInsertClip
import org.slf4j.LoggerFactory
import java.lang.Exception

typealias mongoQuery = org.springframework.data.mongodb.core.query.Query

/**
 * mongo 元数据实体的基类
 */
abstract class MongoBaseEntity<T : IMongoDocument>(val entityClass: Class<T>, entityName: String) : BaseDbEntity(entityName) {
    //    abstract fun getColumns(): Array<String>;
    companion object {
        private val logger by lazy {
            return@lazy LoggerFactory.getLogger(this::class.java)
        }
    }

//    /**
//     * 通过 using 作用域 切换数据源。
//     */
//    val mongoTemplate: MongoTemplate
//        get() {
//            return scopes.getLatestScope<MongoTemplate>() ?: SpringUtil.getBean<MongoTemplate>()
//        }


//    /**
//     * @param entity 实体
//     * @return 返回Id
//     */
//    fun doInsert(entity: Map<String, *>): String {
//        db.affectRowCount = -1;
//        var entity = entity.toMutableMap()
//        if (entity.containsKey("id") == false) {
//            entity.set("id", ObjectId().toString())
//        }
//
//        var retId = "";
//        var error = false;
//        try {
//            mongoTemplate.insert(db.procSetDocumentData(entity), tableName);
//            db.affectRowCount = 1;
//            retId = entity.get("id").toString()
//        } catch (e: Exception) {
//            error = true;
//            throw e;
//        } finally {
//            logger.InfoError(error) { "insert:[" + this.tableName + "],data:" + entity.ToJson() + ",_id:" + retId }
//        }
//
//        return retId
//    }

//    /**
//     * @param entity 实体
//     * @return 返回Id
//     */
//    fun doInsert(entity: T): String {
//        if (entity.id.isEmpty()) {
//            entity.id = ObjectId().toString();
//        }
//
//        var error = false;
//        try {
//            mongoTemplate.insert(entity, tableName);
//            db.affectRowCount = 1;
//        } catch (e: Exception) {
//            error = true;
//            throw e;
//        } finally {
//            logger.InfoError(error) { "insert:[" + this.tableName + "],data:" + entity.ToJson() + ",_id:" + entity.id }
//        }
//
//
//        return entity.id;
//    }

    /**
     * 插入单条实体
     */
    fun doInsert(entity: T): String {
        var batchInsert = MongoBaseInsertClip(this.tableName)
        batchInsert.addEntity(entity)
        batchInsert.exec();
        return entity.id
    }

//    /**
//     * 扩展的aggregate
//     * @param collectionClazz 集合类型
//     * @param pipelines 是指 aggregate命令中 pipeline部分。
//     * @return 返回结果集中  result 集合部分。
//     */
//    fun aggregatePipelineEx(vararg pipelines: String): Array<Document> {
//        var queryJson = """{ aggregate: "${this.tableName}",pipeline: [
//${pipelines.joinToString(",\n")}
//] ,
//cursor: {} } """;
//
//        var result = mongoTemplate.executeCommand(queryJson)
//
//        var ret = mutableListOf<Document>()
//        if (result.getDouble("ok") != 1.0) {
//            return ret.toTypedArray()
//        }
//
//        ((result.get("cursor") as Document).get("firstBatch") as ArrayList<Document>).forEach {
//            ret.add(it)
//        }
//
//        return ret.toTypedArray()
//    }

//    fun save(entity: Any) {
//        this.template.save(entity);
//    }


//    fun aggregation(vararg whereData: Criteria): MongoAggregationClip<T> {
//        var ret = MongoAggregationClip(collectionClass);
//        ret.match(*whereData);
//        return ret;
//    }

    fun getMongoCriteria(vararg where: Criteria): Criteria {
        if (where.size == 0) return Criteria();
        if (where.size == 1) return where[0];
        return Criteria().andOperator(*where);
    }
}


