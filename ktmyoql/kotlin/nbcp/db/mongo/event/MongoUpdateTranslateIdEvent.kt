package nbcp.db.mongo.event;

import nbcp.db.mongo.*;
import nbcp.db.*
import org.springframework.stereotype.Component

/**
 * 同步处理，更新的实体，添加 updateAt 字段。
 */
@Component
class MongoUpdateTranslateIdEvent : IMongoEntityUpdate {
    override fun beforeUpdate(update: MongoBaseUpdateClip,chain:EventChain): EventResult {

        for (kv in update.setData) {
            var value = kv.value;
            if (value != null) {
                value = db.mongo.transformDocumentIdTo_id(kv.value!!)
                update.setData.set(kv.key, value);
            } else {
                update.unsetData.add(kv.key);
            }
        }

        for (kv in update.pushData) {
            var value = db.mongo.transformDocumentIdTo_id(kv.value)
            update.pushData.put(kv.key, value);
        }

        return EventResult(true, null)
    }

    override fun update(update: MongoBaseUpdateClip,chain:EventChain, eventData: EventResult) {
    }
}