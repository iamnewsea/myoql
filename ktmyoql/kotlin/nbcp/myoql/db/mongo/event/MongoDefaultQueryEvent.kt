package nbcp.myoql.db.mongo.event;

import nbcp.base.extend.scopes
import nbcp.myoql.db.comm.EventResult
import nbcp.myoql.db.comm.LogicalDelete
import nbcp.myoql.db.db
import nbcp.myoql.db.enums.MyOqlDbScopeEnum
import nbcp.myoql.db.mongo.MongoBaseQueryClip
import nbcp.myoql.db.mongo.base.MongoColumnName
import org.springframework.stereotype.Component

/**
 * 同步处理，插入的实体，添加 createAt 字段。
 */
@Component
class MongoDefaultQueryEvent : IMongoEntityQuery {
    override fun beforeQuery(query: MongoBaseQueryClip): EventResult {
        if (scopes.getLatest(MyOqlDbScopeEnum.IgnoreLogicalDelete) != null) {
            return EventResult(true, null)
        }

        var moer = db.mongo.mongoEvents.getCollection(query.defEntityName);
        if (moer == null) {
            return EventResult(true, null)
        }

        var logicalDelete = moer::class.java.getAnnotation(LogicalDelete::class.java)

        if (logicalDelete == null) {
            return EventResult(true);
        }
        if (logicalDelete.value.isEmpty()) {
            return EventResult(true);
        }

        query.whereData.putAll((MongoColumnName(logicalDelete.value) match_in arrayOf(false, null)).criteriaObject)

        return EventResult(true);
    }

    override fun query(query: MongoBaseQueryClip, eventData: EventResult) {

    }
}