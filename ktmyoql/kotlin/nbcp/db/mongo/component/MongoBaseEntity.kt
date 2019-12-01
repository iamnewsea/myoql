package nbcp.db.mongo

import com.mongodb.BasicDBObject
import org.bson.Document
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import nbcp.base.extend.*
import nbcp.base.utils.MyUtil
import nbcp.base.utils.SpringUtil
import nbcp.db.db
import nbcp.db.mongo.MongoDeleteClip
import nbcp.db.mongo.MongoQueryClip
import nbcp.db.mongo.MongoUpdateClip

import nbcp.db.mongo.*
import java.util.stream.IntStream

typealias mongoQuery = org.springframework.data.mongodb.core.query.Query

/**
 * mongo 的操作的基类
 */
abstract class MongoBaseEntity<T : IMongoDocument>(val entityClass: Class<T>, entityName: String):BaseDbEntity(entityName) {
//    abstract fun getColumns(): Array<String>;


    var mongoTemplate = SpringUtil.getBean<MongoTemplate>()


    /**
     * @param entity 实体
     * @return 返回Id
     */
    fun insert(entity: BasicDBObject): String {
        if (entity.containsField("id") == false) {
            entity.set("id", ObjectId().toString())
        }

        mongoTemplate.insert(entity, tableName);
        db.affectRowCount = 1;
        return entity.get("id").toString()
    }


    /**
     * 扩展的aggregate
     * @param collectionClazz 集合类型
     * @param pipelines 是指 aggregate命令中 pipeline部分。
     * @return 返回结果集中  result 集合部分。
     */
    fun aggregatePipelineEx(vararg pipelines: String): Array<Document> {
        var queryJson = """{ aggregate: "${this.tableName}",pipeline: [
${pipelines.joinToString(",\n")}
] ,
cursor: {} } """;

        var result = mongoTemplate.executeCommand(queryJson)

        var ret = mutableListOf<Document>()
        if (result.getDouble("ok") != 1.0) {
            return ret.toTypedArray()
        }

        ((result.get("cursor") as Document).get("firstBatch") as ArrayList<Document>).forEach {
            ret.add(it)
        }

        return ret.toTypedArray()
    }


    /**
     * @param entity 实体
     * @return 返回Id
     */
    fun insert(entity: T): String {
        if (entity.id.isEmpty()) {
            entity.id = ObjectId().toString();
        }

        mongoTemplate.insert(entity, tableName);
        db.affectRowCount = 1;

        return entity.id;
    }


    fun insertAll(entities: Collection<T>) {
        entities.forEach {
            if (it.id.isEmpty()) {
                it.id = ObjectId().toString()
            }
        }
        mongoTemplate.insertAll(entities)
        db.affectRowCount =entities.size
    }


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


