package nbcp.db.mongo.event;

import nbcp.comm.scopes
import nbcp.db.mongo.*;
import nbcp.db.*
import org.springframework.stereotype.Component
import java.time.LocalDateTime

/**
 * 同步处理，更新的实体，添加 updateAt 字段。
 */
@Component
class MongoUpdateAtEvent : IMongoEntityUpdate {
    override fun beforeUpdate(update: MongoBaseUpdateClip): EventResult {
        if (scopes.getLatest(MyOqlOrmScope.IgnoreUpdateAt) != null) {
            return EventResult(true, null)
        }

        update.setData.remove("createAt")
        update.setValue("updateAt", LocalDateTime.now())

        return EventResult(true, null)
    }

    override fun update(update: MongoBaseUpdateClip, eventData: EventResult) {
    }
}