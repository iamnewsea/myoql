package nbcp.db.sql.event;

import nbcp.comm.AsString
import nbcp.comm.HasValue
import nbcp.db.sql.*;
import nbcp.db.*
import nbcp.db.mongo.MongoEntityCollector
import org.springframework.stereotype.Component

/**
 * 处理删除数据后转移到垃圾箱功能
 */
@Component
class SqlDefaultDeleteEvent : ISqlEntityDelete {

    override fun beforeDelete(delete: SqlDeleteClip<*>): EventResult {
        return EventResult(true);
    }

    override fun delete(delete: SqlDeleteClip<*>, eventData: EventResult) {
        //破坏缓存
        val cacheGroups = MongoEntityCollector.sysRedisCacheDefines.get(delete.tableName)
        if (cacheGroups == null) {
            return;
        }

        if (delete.whereDatas.hasOrClip()) {
            clearAllCache(delete.tableName);
        }


        cacheGroups.forEach { groupKeys ->
            val groupValue = groupKeys.map {
                return@map it to delete.whereDatas.findValueFromRootLevel(it).AsString()
            }.filter { it.second.HasValue }
                .toMap();

            if (groupValue.keys.size != groupKeys.size) {
                clearAllCache(delete.tableName);
                return;
            }


            db.brokeRedisCache(
                table = delete.tableName,
                groupKey = groupValue.keys.joinToString(","),
                groupValue = groupValue.values.joinToString(",")
            )
        }
    }

    private fun clearAllCache(tableName: String) {
        db.brokeRedisCache(
            table = tableName,
            groupKey = "",
            groupValue = ""
        )
    }
}