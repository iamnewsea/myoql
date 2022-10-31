package nbcp.db.sql.event;

import nbcp.comm.*
import nbcp.db.sql.*;
import nbcp.db.*
import nbcp.db.mongo.MongoEntityCollector
import org.springframework.stereotype.Component
import java.time.LocalDateTime

/**
 * 处理删除数据后转移到垃圾箱功能
 */
@Component
class SqlDefaultUpdateEvent : ISqlEntityUpdate {

    override fun beforeUpdate(update: SqlUpdateClip<*>): EventResult {
        return EventResult(true)
    }

    override fun update(update: SqlUpdateClip<*>, eventData: EventResult) {
        setUpdateAt(update)
        brokeCache(update)
    }

    private fun setUpdateAt(update: SqlUpdateClip<*>) {
        update.setData.removeAll { it.toString() == "createAt" }
        if (scopes.getLatest(MyOqlOrmScope.IgnoreUpdateAt) == null) {
            var updateAtColumn = update.mainEntity.getColumns().getColumn("updateAt")
            if (updateAtColumn != null) {
                update.setData.put(updateAtColumn, LocalDateTime.now())
            }
        }

        //处理 Json
        update.mainEntity.getJsonColumns()
            .forEach { column ->
                var key = update.setData.keys.firstOrNull { it.name == column.getAliasName() }
                if (key == null) {
                    return@forEach
                }

                var value = update.setData.get(key).AsString()
                if (value.isEmpty()) {
                    return@forEach
                }

                update.setData.put(key, value.ToJson())
            }


        //处理 SpreadColumn
        update.mainEntity.getSpreadColumns()
            .forEach { spread ->
                var key = update.setData.keys.firstOrNull { it.name == spread.column }
                if (key == null) {
                    return@forEach
                }
                var value = update.setData.get(key)?.ConvertType(Map::class.java) as Map<String, Any?>?;
                if (value == null) {
                    return@forEach
                }

                var all_columns = update.mainEntity.getColumns()
                value.keys.forEach { key ->
                    var column = all_columns.getColumn(spread.getPrefixName() + key);
                    if (column == null) {
                        return@forEach
                    }
                    //仅支持简单一级Json！
                    update.setData.set(column, value.get(key))
                }

                update.setData.removeAll { it.name == spread.column }
            }
    }

    private fun brokeCache(update: SqlUpdateClip<*>) {
        val cacheGroups = MongoEntityCollector.sysRedisCacheDefines.get(update.tableName)
        if (cacheGroups == null) {
            return;
        }

        if (update.whereDatas.hasOrClip()) {
            clearAllCache(update.tableName);
            return;
        }

        cacheGroups.forEach { groupKeys ->
            val groupValue = groupKeys.map {
                return@map it to update.whereDatas.findValueFromRootLevel(it).AsString()
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
    }

    private fun clearAllCache(tableName: String) {
        db.brokeRedisCache(
            table = tableName,
            groupKey = "",
            groupValue = ""
        )
    }
}