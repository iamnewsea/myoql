package nbcp.db.mongo.event;

import nbcp.comm.AllFields
import nbcp.db.mongo.*;
import nbcp.comm.AsString
import nbcp.comm.IsStringType
import nbcp.db.*
import org.bson.types.ObjectId
import org.springframework.stereotype.Component
import java.time.LocalDateTime

/**
 * 同步处理，插入的实体，添加 createAt 字段。
 */
@Component
class MongoInsertEvent : IMongoEntityInsert {
    override fun beforeInsert(insert: MongoBaseInsertClip): EventResult {
        insert.entities.forEach { entity ->
//            db.fillCityName(entity);

            if (entity is BaseEntity) {
                if (entity.id.isEmpty()) {
                    entity.id = ObjectId().toString()
                }

                entity.createAt = LocalDateTime.now();
            } else if (entity is MutableMap<*, *>) {
                var map = entity as MutableMap<String, Any?>
                if (map.get("_id").AsString().isNullOrEmpty()) {
                    map.set("_id", ObjectId().toString())
                }
                map.set("createAt", LocalDateTime.now())
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
            }
        }

        return EventResult(true, null);
    }

    override fun insert(insert: MongoBaseInsertClip, eventData: EventResult) {

    }

}