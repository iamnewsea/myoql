package nbcp.db.mongo.event;

import nbcp.comm.*
import nbcp.db.mongo.*;
import nbcp.db.*
import nbcp.db.mongo.entity.SysLastSortNumber
import org.bson.types.ObjectId
import org.springframework.stereotype.Component
import java.time.LocalDateTime

/**
 * 同步处理，插入的实体，添加 createAt 字段。
 */
@Component
class MongoDefaultInsertEvent : IMongoEntityInsert {
    override fun beforeInsert(insert: MongoBaseInsertClip): EventResult {
        var sortNumber: SortNumber? = null;
        var tableName = insert.actualTableName;
        if (insert is MongoInsertClip<*>) {
            sortNumber = insert.moerEntity::class.java.getAnnotation(SortNumber::class.java)
        }

        insert.entities.forEach { entity ->
//            db.fillCityName(entity);

            if (entity is BaseEntity) {
                if (entity.id.isEmpty()) {
                    entity.id = ObjectId().toString()
                }

                entity.createAt = LocalDateTime.now();

                if (sortNumber != null) {
                    var field = entity::class.java.FindField(sortNumber.field);
                    if (field != null) {
                        if (field.get(entity).AsFloat() == 0F) {
                            field.set(entity, getSortNumber(sortNumber, tableName))
                        }
                    }
                }
            } else if (entity is MutableMap<*, *>) {
                var map = entity as MutableMap<String, Any?>
                if (map.get("id").AsString().isNullOrEmpty()) {
                    map.set("id", ObjectId().toString())
                }
                map.set("createAt", LocalDateTime.now())

                if (sortNumber != null) {
                    if (map.get(sortNumber.field).AsFloat() == 0F) {
                        map.put(sortNumber.field, getSortNumber(sortNumber, tableName))
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


                if (sortNumber != null) {
                    var field = entity::class.java.FindField(sortNumber.field);
                    if (field != null) {
                        if (field.get(entity).AsFloat() == 0F) {
                            field.set(entity, getSortNumber(sortNumber, tableName))
                        }
                    }
                }
            }

            //设置实体内的 _id
            db.mongo.transformDocumentIdTo_id(entity);
        }

        return EventResult(true, null);
    }

    private fun getSortNumber(sortNumber: SortNumber, tableName: String): Float {

        db.mor_base.sysLastSortNumber.update()
                .where { it.table match tableName }
                .where { it.group match sortNumber.groupBy }
                .inc { it.value op_inc sortNumber.step }
                .saveAndReturnNew();
        return 0F
    }

    override fun insert(insert: MongoBaseInsertClip, eventData: EventResult) {

    }

}