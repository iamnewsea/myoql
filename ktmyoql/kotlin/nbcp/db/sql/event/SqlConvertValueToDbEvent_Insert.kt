package nbcp.db.sql

import nbcp.comm.AllFields
import nbcp.comm.ToJson
import nbcp.utils.*
import nbcp.db.*
import nbcp.db.sql.entity.s_dustbin
import org.springframework.stereotype.Component
import java.lang.reflect.Field
import kotlin.reflect.full.createInstance

/**
 *
 */
@Component
class SqlConvertValueToDbEvent_Insert : ISqlEntityInsert {
    override fun beforeInsert(insert: SqlInsertClip<*, *>): DbEntityEventResult? {

        var annotations = mutableMapOf<Field, IConverter>()
        insert.mainEntity.tableClass.AllFields.forEach {
            var ann = it.getAnnotation(ConverterValueToDb::class.java);
            if (ann != null) {
                annotations.put(it, ann.converter.createInstance());
            }
        }

        annotations.forEach { field, converter ->
            insert.entities.forEach {
                it.set(field.name, converter.convert(it.get(field.name)));
            }
        }


        return DbEntityEventResult(true)
    }

    override fun insert(insert: SqlInsertClip<*, *>, eventData: DbEntityEventResult?) {

    }
}