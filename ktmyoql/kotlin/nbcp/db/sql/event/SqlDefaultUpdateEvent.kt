package nbcp.db.sql.event;

import nbcp.comm.AsString
import nbcp.comm.HasValue
import nbcp.db.sql.*;
import nbcp.comm.ToJson
import nbcp.utils.*
import nbcp.db.*
import nbcp.db.mongo.MongoEntityCollector
import nbcp.db.sql.entity.s_dustbin
import org.springframework.stereotype.Component

/**
 * 处理删除数据后转移到垃圾箱功能
 */
@Component
class SqlDefaultUpdateEvent : ISqlEntityUpdate {

    override fun beforeUpdate(update: SqlUpdateClip<*>): EventResult {
        return EventResult(true)
    }

    override fun update(update: SqlUpdateClip<*>, eventData: EventResult) {
        val groupKeys = MongoEntityCollector.sysRedisCacheDefines.get(update.tableName)

        if (groupKeys == null) {
            return;
        }

        if (update.whereDatas.hasOrClip()) {
            clearAllCache(update.tableName);
            return;
        }

        val groupValue = groupKeys.map {
            return@map it to update.whereDatas.findRootWhere(it).AsString()
        }.filter { it.second.HasValue }
            .toMap();

        if (groupValue.keys.size != groupKeys.size) {
            clearAllCache(update.tableName);
            return;
        }

        db.brokeRedisCache(
            table = update.tableName,
            groupKey = groupValue.keys.joinToString(","),
            groupValue = groupValue.values.joinToString(",")
        )
    }

    private fun clearAllCache(tableName: String) {
        db.brokeRedisCache(
            table = tableName,
            groupKey = "",
            groupValue = ""
        )
    }
}