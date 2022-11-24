package nbcp.myoql.db.mongo.event;

import nbcp.base.extend.*
import nbcp.base.utils.MyUtil
import nbcp.myoql.db.BaseEntity
import nbcp.myoql.db.comm.EventResult
import nbcp.myoql.db.comm.SortNumber
import nbcp.myoql.db.comm.op_inc
import nbcp.myoql.db.db
import nbcp.myoql.db.mongo.MongoBaseInsertClip
import nbcp.myoql.db.mongo.MongoEntityCollector
import nbcp.myoql.db.mongo.update
import org.bson.types.ObjectId
import org.springframework.stereotype.Component
import java.time.LocalDateTime

/**
 * 同步处理，插入的实体，添加 createAt 字段。
 */
@Component
class MongoDefaultInsertEvent : IMongoEntityInsert {
    override fun beforeInsert(insert: MongoBaseInsertClip): EventResult {
        var sortNumbers = arrayOf<SortNumber>()
        var tableName = insert.actualTableName;

        var table = db.mongo.mongoEvents.getCollection(insert.defEntityName)
        if (table != null) {
            sortNumbers = table::class.java.getAnnotationsByType(SortNumber::class.java)
        }

        insert.entities.forEach { entity ->
//            db.fillCityName(entity);

            if (entity is BaseEntity) {
                if (entity.id.isEmpty()) {
                    entity.id = ObjectId().toString()
                }

                entity.createAt = LocalDateTime.now();

                proc_sortNumber(sortNumbers, entity, tableName)
            } else if (entity is MutableMap<*, *>) {
                var map = entity as MutableMap<String, Any?>
                if (map.get("id").AsString().isNullOrEmpty()) {
                    map.set("id", ObjectId().toString())
                }
                map.set("createAt", LocalDateTime.now())

                sortNumbers.forEach { sortNumber ->
                    if (map.getTypeValue<Float>(sortNumber.field, ignoreCase = false).AsFloat() == 0F) {
                        var groupBy = MyUtil.getValueByWbsPath(entity, sortNumber.groupBy).AsString();
                        var sortNumberValue = getSortNumber(tableName, groupBy, sortNumber.step);
                        if (sortNumberValue != null) {
                            map.setValueByWbsPath(
                                sortNumber.field,
                                ignoreCase = false,
                                value = sortNumberValue
                            )
                        }
                    }
                }

            } else {
                //反射两个属性 id,createAt
                var entityClassFields = entity.javaClass.AllFields
                var idField = entityClassFields.firstOrNull { it.name == "id" }
                if (idField != null && idField.type.IsStringType) {
                    var idValue = idField.get(entity).AsString();
                    if (idValue.isEmpty()) {
                        idField.set(entity, ObjectId().toString())
                    }
                }

                val createAtField = entityClassFields.firstOrNull { it.name == "createAt" }
                if (createAtField != null) {
                    val createAtValue = createAtField.get(entity);
                    if (createAtValue == null) {
                        createAtField.set(entity, LocalDateTime.now())
                    }
                }


                proc_sortNumber(sortNumbers, entity, tableName)
            }

            //设置实体内的 _id
            db.mongo.transformDocumentIdTo_id(entity);
        }

        return EventResult(true, null);
    }

    private fun proc_sortNumber(
        sortNumbers: Array<SortNumber>,
        entity: Any,
        tableName: String
    ) {
        sortNumbers.forEach { sortNumber ->
            var value = MyUtil.getValueByWbsPath(entity, *sortNumber.field.split(".").toTypedArray());
            if (value != null) {
                if (value.AsFloat() == 0F) {
                    var groupBy = MyUtil.getValueByWbsPath(entity, sortNumber.groupBy).AsString();
                    var sortNumberValue = getSortNumber(tableName, groupBy, sortNumber.step);
                    if (sortNumberValue != null) {

                        MyUtil.setValueByWbsPath(
                            entity,
                            *sortNumber.field.split(".").toTypedArray(),
                            ignoreCase = false,
                            value = sortNumberValue
                        )

                    }
                }
            }
        }
    }

    private fun getSortNumber(tableName: String, groupBy: String, step: Number): Float? {
        return db.morBase.sysLastSortNumber.update()
            .where { it.table match tableName }
            .where { it.group match groupBy }
            .inc { it.value op_inc step }
            .saveAndReturnNew()
            ?.value
    }

    override fun insert(insert: MongoBaseInsertClip, eventData: EventResult) {
        //清缓存
        val cacheGroups = MongoEntityCollector.sysRedisCacheDefines.get(insert.defEntityName)
        if (cacheGroups == null) {
            return;
        }

        insert.entities.forEach { ent ->

            cacheGroups.forEach { groupKeys ->


                val groupValue = groupKeys.map {
                    return@map it to MyUtil.getValueByWbsPath(ent, it).AsString()
                }.filter { it.second.HasValue }
                    .toMap();

                if (groupValue.keys.size != groupKeys.size) {
                    clearAllCache(insert.actualTableName);
                    return;
                }

                db.brokeRedisCache(
                    table = insert.actualTableName,
                    groupKey = groupValue.keys.joinToString(","),
                    groupValue = groupValue.values.joinToString(",")
                )
            }
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