package nbcp.db.mongo

import nbcp.base.extend.NoAffectRowCount
import nbcp.base.extend.getStringValue
import nbcp.base.extend.using
import nbcp.base.utils.MyUtil
import nbcp.db.*
import nbcp.db.mongo.MongoDeleteClip
import nbcp.db.mongo.MongoEntityEvent
import nbcp.db.mongo.component.MongoBaseUpdateClip
import nbcp.db.mongo.entity.SysDustbin
import nbcp.db.mongo.table.MongoBaseGroup
import nbcp.db.mongo.toDocument
import org.bson.Document
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.query.BasicQuery
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