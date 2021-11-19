package nbcp.db.mongo.event;

import nbcp.comm.AsString
import nbcp.db.mongo.*;
import nbcp.db.*
import nbcp.db.mongo.entity.*
import org.bson.Document
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.query.BasicQuery
import org.springframework.stereotype.Component
import java.io.Serializable


/**
 * 同步处理，删除的数据转移到垃圾箱
 */
@Component
class MongoDustbinEvent : IMongoEntityDelete {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    override fun beforeDelete(delete: MongoDeleteClip<*>): EventResult {
        var contains = MongoEntityCollector.dustbinEntitys.contains(delete.moerEntity.entityClass)
        if (contains == false) {
            return EventResult(true, null);
        }

        //找出数据
        var where = delete.getMongoCriteria(*delete.whereData.toTypedArray());
        var query = MongoBaseQueryClip(delete.collectionName);
        query.whereData.add(where);
        var list = query.toList(Document::class.java)

        return EventResult(true, list)
    }

    override fun delete(delete: MongoDeleteClip<*>, eventData: EventResult) {
        val list = eventData.extData as List<Document>?
        if (list.isNullOrEmpty()) return;

        val dustbin = SysDustbin()
//        dustbin.id = ObjectId().toString()
        dustbin.table = delete.collectionName
        dustbin.data = list as Serializable?;
        db.mor_base.sysDustbin.doInsert(dustbin)

        val list_ids = list.map { it.getString("_id").AsString() }

        logger.info("${delete.collectionName}.${list_ids.joinToString(",")} 进了垃圾桶")
    }
}