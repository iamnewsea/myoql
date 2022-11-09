package nbcp.myoql.db.mongo;

import nbcp.base.extend.AsString
import nbcp.myoql.db.comm.EventResult
import nbcp.myoql.db.db
import nbcp.myoql.db.mongo.entity.SysDustbin
import nbcp.myoql.db.mongo.event.IMongoEntityDelete
import org.bson.Document
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.Serializable


/**
 * 同步处理，删除的数据转移到垃圾箱
 */
@Component
class MongoDeleteWithDustbinEvent : IMongoEntityDelete {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    override fun beforeDelete(delete: MongoDeleteClip<*>): EventResult {
        var contains = MongoEntityCollector.dustbinEntities.any { it.actualTableName == delete.actualTableName }
        if (contains == false) {
            return EventResult(true, null);
        }

        //找出数据
        var where = db.mongo.getMergedMongoCriteria(delete.whereData);
        var query = MongoBaseQueryClip(delete.actualTableName);
        query.whereData.putAll(where.criteriaObject);
        var list = query.toList(Document::class.java)

        return EventResult(true, list)
    }

    override fun delete(delete: MongoDeleteClip<*>, eventData: EventResult) {
        val list = eventData.extData as List<Document>?
        if (list.isNullOrEmpty()) return;

        val dustbin = SysDustbin()
        dustbin.table = delete.actualTableName
        dustbin.data = list as Serializable?;
        db.morBase.sysDustbin.doInsert(dustbin)

        val list_ids = list.map { it.get("_id").AsString() }

        logger.info("${delete.actualTableName}.${list_ids.joinToString(",")} 进了垃圾桶")
    }
}