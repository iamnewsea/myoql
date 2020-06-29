package nbcp.db.mongo

import nbcp.comm.AsString
import nbcp.db.*
import nbcp.utils.RecursionUtil
import org.bson.types.ObjectId
import org.springframework.stereotype.Component
import java.time.LocalDateTime

/**
 * 同步处理，插入的实体，添加 createAt 字段。
 */
@Component
class MongoInsertEvent : IMongoEntityInsert {
    override fun beforeInsert(insert: MongoBaseInsertClip): DbEntityEventResult {
        insert.entities.forEach { entity ->
            db.fillCityName(entity);


            if (entity is IMongoDocument) {
                if (entity.id.isEmpty()) {
                    entity.id = ObjectId().toString()
                }

                entity.createAt = LocalDateTime.now();
            } else if (entity is MutableMap<*, *>) {
                var map = entity as MutableMap<String, Any?>
                if (map.get("id").AsString().isNullOrEmpty()) {
                    map.set("id", ObjectId().toString())
                }
                map.set("createAt", LocalDateTime.now())
            }
        }

        return DbEntityEventResult(true, null);
    }

    override fun insert(insert: MongoBaseInsertClip, eventData: DbEntityEventResult) {

    }

}