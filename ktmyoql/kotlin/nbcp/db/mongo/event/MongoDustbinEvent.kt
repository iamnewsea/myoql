package nbcp.db.mongo.event;

import nbcp.db.mongo.*;
import nbcp.db.*
import nbcp.db.mongo.entity.*
import org.bson.Document
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.query.BasicQuery
import org.springframework.stereotype.Component


/**
 * 同步处理，删除的数据转移到垃圾箱
 */
@Component
class MongoDustbinEvent : IMongoEntityDelete {
    override fun beforeDelete(delete: MongoDeleteClip<*>): EventResult {
        var contains = MongoEntityEvent.dustbinEntitys.contains(delete.moerEntity.entityClass)
        if (contains == false) {
            return EventResult(true, null);
        }

        //找出数据
        var where = delete.getMongoCriteria(*delete.whereData.toTypedArray());
        var query = BasicQuery(where.toDocument())
        var cursor = delete.mongoTemplate.find(query, Document::class.java, delete.collectionName)
        return EventResult(true, cursor)
    }

    override fun delete(delete: MongoDeleteClip<*>, eventData: EventResult) {
        var data = eventData.extData
        if (data == null) return

        var dustbin = SysDustbin()
        dustbin.id = ObjectId().toString()
        dustbin.table = delete.collectionName
        dustbin.data = data;
        db.mor_base.sysDustbin.doInsert(dustbin)
    }
}