package nbcp.db.sql

import nbcp.comm.AllFields
import nbcp.utils.*
import nbcp.db.*
import org.springframework.stereotype.Component
import java.lang.reflect.Field
import kotlin.reflect.full.createInstance

/**
 *
 */
@Component
class SqlConvertValueToDbEvent_Insert : ISqlEntityInsert {
    override fun beforeInsert(insert: SqlInsertClip<*, *>): EventResult? {

        var annotations = mutableMapOf<Field, IConverter>()
        insert.mainEntity.tableClass.AllFields.forEach {
            var ann = it.getAnnotation(ConverterValueToDb::class.java);
            if (ann != null) {
                annotations.put(it, ann.converter.createInstance());
            }
        }

        annotations.forEach { field, converter ->

            var values = mutableListOf<Any?>()

            insert.entities.forEach {
                var convertedValue = converter.convert(it.get(field.name))
                values.add(convertedValue);
                it.set(field.name, convertedValue);
            }

            insert.ori_entities.forEachIndexed { index, entity ->
                MyUtil.setPrivatePropertyValue(entity, field.name, values[index])
            }
        }


        return EventResult(true)
    }

    override fun insert(insert: SqlInsertClip<*, *>, eventData: EventResult?) {

    }
}