package nbcp.db.mongo

import nbcp.db.*
import nbcp.db.mongo.component.MongoBaseUpdateClip
import org.springframework.stereotype.Component
import java.time.LocalDateTime

/**
 * 同步处理，更新的实体，添加 updateAt 字段。
 */
@Component
class MongoUpdateAtEvent : IMongoEntityUpdate {
    override fun beforeUpdate(update: MongoBaseUpdateClip): DbEntityEventResult {
        update.setValue("updateAt", LocalDateTime.now())
        return DbEntityEventResult(true, null)
    }

    override fun update(update: MongoBaseUpdateClip, eventData: DbEntityEventResult) {
    }
}