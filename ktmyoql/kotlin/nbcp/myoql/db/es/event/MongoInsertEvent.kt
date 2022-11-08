package nbcp.myoql.db.es.event;

import nbcp.base.comm.*;
import nbcp.base.db.*;
import nbcp.base.enums.*;
import nbcp.base.extend.*;
import nbcp.base.utils.*;
import nbcp.myoql.db.enums.*
import nbcp.myoql.db.*;
import nbcp.myoql.db.*
import nbcp.db.es.*
import nbcp.myoql.db.comm.EventResult
import nbcp.myoql.db.es.component.EsBaseInsertClip
import org.springframework.stereotype.Component
import java.time.LocalDateTime

/**
 * 同步处理，插入的实体，添加 createAt 字段。
 */
@Component
class EsInsertEvent : IEsEntityInsert {
    override fun beforeInsert(insert: EsBaseInsertClip): EventResult {
        insert.entities.forEach { entity ->

            if (entity is BaseEntity) {
                if (entity.id.isEmpty()) {
                    entity.id = CodeUtil.getCode()
                }

                entity.createAt = LocalDateTime.now();
            } else if (entity is MutableMap<*, *>) {
                var map = entity as MutableMap<String, Any?>
                if (map.get("id").AsString().isNullOrEmpty()) {
                    map.set("id", CodeUtil.getCode())
                }
                map.set("createAt", LocalDateTime.now())
            } else {
                //反射两个属性 id,createAt
                var entityClassFields = entity.javaClass.AllFields
                var idField = entityClassFields.firstOrNull { it.name == "id" }
                if (idField != null && idField.type.IsStringType) {
                    var idValue = idField.get(entity).AsString();
                    if (idValue.isEmpty()) {
                        idField.set(entity, CodeUtil.getCode())
                    }
                }

                var createAtField = entityClassFields.firstOrNull { it.name == "createAt" }
                if (createAtField != null) {
                    var createAtValue = createAtField.get(entity);
                    if (createAtValue == null) {
                        createAtField.set(entity, LocalDateTime.now())
                    }
                }
            }
        }

        return EventResult(true, null);
    }

    override fun insert(insert: EsBaseInsertClip, eventData: EventResult) {

    }

}