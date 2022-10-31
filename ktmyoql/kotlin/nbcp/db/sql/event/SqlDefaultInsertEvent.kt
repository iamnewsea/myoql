package nbcp.db.sql.event;

import nbcp.comm.*
import nbcp.db.sql.*;
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
                if (field == null) {
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


        //处理 Json 类型的数据
        insert.mainEntity.getJsonColumns()
            .forEach { column ->
                insert.entities.forEach { ent ->
                    val v = ent.get(column.name)
                    if (v != null) {
                        var v_type = v::class.java
                        if (v_type.IsStringType == false) {
                            ent.put(column.name, v.ToJson())
                        }
                    }
                }
            }


        //把 布尔值 改为 1,0
        insert.mainEntity.entityClass.AllFields
            .forEach { field ->
                insert.entities.forEach { ent ->
                    var key = field.name;
                    var value = ent.get(key);
                    if (value == null) {
                        ent.remove(key);
                        return@forEach
                    }

                    ent.set(key, proc_value(value));
                }
            }


        // 处理 SpreadColumn
        insert.mainEntity.getSpreadColumns()
            .forEach { spread ->
                insert.entities.forEach { entity ->
                    val value = entity.get(spread.column)?.ConvertType(Map::class.java) as Map<String, Any?>?;
                    if (value == null) {
                        return@forEach
                    }

                    value.keys.forEach { key ->
                        entity.set(spread.getPrefixName() + key, value.get(key))
                    }

                    entity.remove(spread.column)
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