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
class MongoDefaultUpdateEvent : IMongoEntityUpdate {
    override fun beforeUpdate(update: MongoBaseUpdateClip, chain: EventChain): EventResult {
        setId2_id(update)

        setUpdateAt(update)

        return EventResult(true, null)
    }

    private fun setUpdateAt(update: MongoBaseUpdateClip) {
        update.setData.remove("createAt")
        if (scopes.getLatest(MyOqlOrmScope.IgnoreUpdateAt) == null) {
            update.setValue("updateAt", LocalDateTime.now())
        }
    }

    private fun setId2_id(update: MongoBaseUpdateClip) {
        for (kv in update.setData) {
            var value = kv.value;
            if (value != null) {
                value = db.mongo.transformDocumentIdTo_id(kv.value!!)
                update.setData.set(kv.key, value);
            } else {
                update.unsetColumns.add(kv.key);
            }
        }

        for (kv in update.pushData) {
            var value = db.mongo.transformDocumentIdTo_id(kv.value)
            update.pushData.put(kv.key, value);
        }
    }

    override fun update(update: MongoBaseUpdateClip, chain: EventChain, eventData: EventResult) {
        //清缓存
        var clearAll = false;
        val groupKeys = MongoEntityCollector.sysRedisCacheDefines.get(update.collectionName) ?: arrayOf()
        groupKeys.forEach { groupKey ->
            if (clearAll) return@forEach

            val groupValue = update.whereData.get(groupKey)
            if (groupValue != null) {
                db.brokeRedisCache(
                    table = update.actualTableName,
                    groupKey = groupKey,
                    groupValue = groupValue.toString()
                )
            }
            else {
                clearAll = true;
            }
        }

        if (clearAll) {
            db.brokeRedisCache(
                table = update.actualTableName,
                groupKey = "",
                groupValue = ""
            )
        }
    }
}