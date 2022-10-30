package nbcp.db.sql.event;

import nbcp.comm.AsString
import nbcp.comm.FindField
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
class SqlDefaultInsertEvent : ISqlEntityInsert {


    override fun beforeInsert(insert: nbcp.db.sql.SqlInsertClip<*, *>): nbcp.db.EventResult {
        //先处理 SqlAutoIncrementKey
        insert.mainEntity.getAutoIncrementKey()
            .apply {
                if (this.HasValue) {
                    insert.entities
                        .forEach { ent ->
                            ent.remove(this);
                        }
                }
            }

        //再处理 AutoId
        insert.mainEntity::class.java.getAnnotationsByType(ConverterValueToDb::class.java)
            .forEach { converterClass ->
                var converter = converterClass.value.java.getConstructor().newInstance()
                var field = insert.mainEntity.entityClass.FindField(converterClass.field);
                if( field == null){
                    throw java.lang.RuntimeException("实体:${insert.mainEntity.entityClass.simpleName} 定义的 ConverterValueToDb 找不到字段: ${converterClass.field} !")
                }
                field.isAccessible = true;

                insert.entities.forEach { ent ->
                    val v = ent.get(converterClass.field)
                    if (v == null || v == "" || v == 0) {
                        ent.put(converterClass.field, converter.convert(field, null))
                    }
                }
            }

        return EventResult(true)
    }

    override fun insert(insert: SqlInsertClip<*, *>, eventData: EventResult) {
        brokeCache(insert)
    }

    private fun brokeCache(insert: SqlInsertClip<*, *>) {
        val cacheGroups = MongoEntityCollector.sysRedisCacheDefines.get(insert.tableName)
        if (cacheGroups == null) {
            return;
        }

        cacheGroups.forEach { groupKeys ->
            insert.entities.forEach { ent ->
                val groupValue = groupKeys.map {
                    return@map it to MyUtil.getValueByWbsPath(ent, it).AsString()
                }.filter { it.second.HasValue }
                    .toMap();

                if (groupValue.keys.size != groupKeys.size) {
                    clearAllCache(insert.tableName);
                    return;
                }


                db.brokeRedisCache(
                    table = insert.tableName,
                    groupKey = groupValue.keys.joinToString(","),
                    groupValue = groupValue.values.joinToString(",")
                )
            }
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