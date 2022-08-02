package nbcp.db.sql.event

import nbcp.db.sql.*;
import nbcp.comm.AllFields
import nbcp.db.*
import org.springframework.stereotype.Component

/**
 * 处理删除数据后转移到垃圾箱功能
 */
@Component
class SqlSpreadColumnEventForInsert : ISqlEntityInsert {
    override fun beforeInsert(insert: SqlInsertClip<*, *>): EventResult {

//        var annotations = mutableListOf<Field>()
//        insert.mainEntity.tableClass.AllFields.forEach {
//            var ann = it.getAnnotation(SqlSpreadColumn::class.java);
//            if (ann != null) {
//                annotations.add(it);
//            }
//        }

        insert.mainEntity.getSpreadColumns().forEach { column ->
            insert.entities.forEach { entity ->
                val value = entity.get(column) as Map<String, *>?;
                if (value == null) {
                    return@forEach
                }

                value.keys.forEach { key->
                    entity.set(column + "_" + key,  value.get(key))
                }
            }
        }


        return EventResult(true)
    }

    override fun insert(insert: SqlInsertClip<*, *>, eventData: EventResult) {

    }
}