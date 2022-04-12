package nbcp.db.mongo.event;

import nbcp.comm.AsString
import nbcp.comm.HasValue
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
    override fun beforeUpdate(update: MongoBaseUpdateClip): EventResult {
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
        db.mongo.transformDocumentIdTo_id(update.setData)

        if (update.setData.containsKey("_id")) {
            throw RuntimeException("不允许更新 id/_id 列!")
        }

        db.mongo.transformDocumentIdTo_id(update.pushData)

        for (kv in update.setData) {
            var value = kv.value;
            if (value == null) {
                update.setData.remove(kv.key);
                update.unsetColumns.add(kv.key);
            }
        }
    }

    override fun update(update: MongoBaseUpdateClip, eventData: EventResult) {
        //清缓存
        val cacheGroups = MongoEntityCollector.sysRedisCacheDefines.get(update.defEntityName)
        if (cacheGroups == null) {
            return;
        }

        if (db.mongo.hasOrClip(update.whereData)) {
            clearAllCache(update.actualTableName);
            return;
        }
        cacheGroups.forEach { groupKeys ->
            val groupValue = groupKeys.map {
                return@map it to update.whereData.findValueFromRootLevel(it).AsString()
            }.filter { it.second.HasValue }
                .toMap();

            if (groupValue.keys.size != groupKeys.size) {
                clearAllCache(update.actualTableName);
                return;
            }

            db.brokeRedisCache(
                table = update.actualTableName,
                groupKey = groupValue.keys.joinToString(","),
                groupValue = groupValue.values.joinToString(",")
            )
        }
    }

    private fun clearAllCache(actualTableName: String) {
        db.brokeRedisCache(
            table = actualTableName,
            groupKey = "",
            groupValue = ""
        )
    }
}