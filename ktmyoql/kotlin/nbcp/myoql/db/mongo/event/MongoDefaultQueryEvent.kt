package nbcp.myoql.db.mongo.event;

import nbcp.base.comm.*;
import nbcp.base.db.*;
import nbcp.base.enums.*;
import nbcp.base.extend.*;
import nbcp.base.utils.*;
import nbcp.myoql.db.enums.*
import nbcp.myoql.db.*;
import nbcp.myoql.db.comm.*
import nbcp.myoql.db.mongo.MongoBaseQueryClip
import nbcp.myoql.db.mongo.base.MongoColumnName
import org.bson.types.ObjectId
import org.springframework.stereotype.Component
import java.time.LocalDateTime

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