package nbcp.db.mongo

import nbcp.db.*
import nbcp.db.mongo.MongoDeleteClip
import nbcp.db.mongo.MongoEntityEvent
import nbcp.db.mongo.entity.SysDustbin
import nbcp.db.mongo.table.BaseGroup
import nbcp.db.mongo.toDocument
import org.bson.Document
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.query.BasicQuery


@DbEntityDelete()
class MongoDustbinEvent : IMongoEntityDelete {

    override fun beforeDelete(delete: MongoDeleteClip<*>): DbEntityEventResult? {
        var dust = delete.moerEntity.entityClass.getAnnotation(MongoEntitySysDustbin::class.java)
        if (dust != null) {
            //找出数据
            var where = delete.getMongoCriteria(*delete.whereData.toTypedArray());
            var query = BasicQuery(where.toDocument())
            var cursor = delete.mongoTemplate.find(query, Document::class.java, delete.collectionName)
            return DbEntityEventResult(true, cursor)
        }

        return null;
    }

    override fun delete(delete: MongoDeleteClip<*>, eventData: DbEntityEventResult?) {
        var data = eventData?.extData
        if (data == null) return

        var dustbin = SysDustbin()
        dustbin.id = ObjectId().toString()
        dustbin.table = delete.collectionName
        dustbin.data = data;
        BaseGroup.SysDustbinEntity().doInsert(dustbin)
    }
}