package nbcp.db.sql

import nbcp.comm.AllFields
import nbcp.db.*
import org.springframework.stereotype.Component

/**
 * 处理删除数据后转移到垃圾箱功能
 */
@Component
class SqlSpreadColumnEvent_Insert : ISqlEntityInsert {
    override fun beforeInsert(insert: SqlInsertClip<*, *>): EventResult? {

//        var annotations = mutableListOf<Field>()
//        insert.mainEntity.tableClass.AllFields.forEach {
//            var ann = it.getAnnotation(SqlSpreadColumn::class.java);
//            if (ann != null) {
//                annotations.add(it);
//            }
//        }

        insert.mainEntity.getSpreadColumns().forEach { column ->
            var field = insert.mainEntity.tableClass.getDeclaredField(column)
            field.isAccessible = true;

            insert.entities.forEach { entity ->
                var value = entity.get(field.name) as Map<String, *>;
                if (value == null) {
                    return@forEach
                }

                field.type.AllFields.forEach {
                    entity.set(field.name + "_" + it.name,  value.get(it.name))
                }
            }
        }


        return EventResult(true)
    }

    override fun insert(insert: SqlInsertClip<*, *>, eventData: EventResult?) {

    }
}