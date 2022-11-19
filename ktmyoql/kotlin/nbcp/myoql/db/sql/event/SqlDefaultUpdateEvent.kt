package nbcp.myoql.db.sql.event;


import nbcp.base.extend.*
import nbcp.myoql.db.comm.EventResult
import nbcp.myoql.db.db
import nbcp.myoql.db.enums.MyOqlDbScopeEnum
import nbcp.myoql.db.mongo.MongoEntityCollector
import nbcp.myoql.db.sql.component.SqlUpdateClip
import org.springframework.stereotype.Component
import java.time.LocalDateTime

/**
 * 处理删除数据后转移到垃圾箱功能
 */
@Component
class SqlDefaultUpdateEvent : ISqlEntityUpdate {

    override fun beforeUpdate(update: SqlUpdateClip<*>): EventResult {

        //处理 Json
        update.mainEntity.getJsonColumns()
            .forEach { column ->
                var key = update.setData.keys.firstOrNull { it.name == column.getAliasName() }
                if (key == null) {
                    return@forEach
                }

                var value = update.setData.get(key)
                if (value == null) {
                    return@forEach
                }


                var v_type = value::class.java
                if (v_type.IsStringType == false) {
                    update.setData.put(key, value.ToJson())
                }
            }

        //还要处理Where条件里的Json


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


        //还要处理Where条件里的SpreadColumn

        return EventResult(true)
    }

    override fun update(update: SqlUpdateClip<*>, eventData: EventResult) {
        setUpdateAt(update)
        brokeCache(update)
    }

    private fun setUpdateAt(update: SqlUpdateClip<*>) {
        update.setData.removeAll { it.toString() == "createAt" }
        if (scopes.getLatest(MyOqlDbScopeEnum.IGNORE_UPDATE_AT) == null) {
            var updateAtColumn = update.mainEntity.getColumns().getColumn("updateAt")
            if (updateAtColumn != null) {
                update.setData.put(updateAtColumn, LocalDateTime.now())
            }
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