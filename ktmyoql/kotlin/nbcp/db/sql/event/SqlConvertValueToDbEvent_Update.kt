package nbcp.db.sql

import nbcp.comm.AllFields
import nbcp.db.*
import org.springframework.stereotype.Component
import java.lang.reflect.Field
import kotlin.reflect.full.createInstance

/**
 *
 */
@Component
class SqlConvertValueToDbEvent_Update : ISqlEntityUpdate {
    override fun beforeUpdate(update: SqlUpdateClip<*, *>): EventResult? {
        var annotations = mutableMapOf<Field, IConverter>()
        update.mainEntity.tableClass.AllFields.forEach {
            var ann = it.getAnnotation(ConverterValueToDb::class.java);
            if (ann != null) {
                annotations.put(it, ann.converter.createInstance());
            }
        }

        annotations.forEach { field, converter ->
            update.sets.forEach {
                if (it.key.name == field.name) {
                    update.sets.put(it.key, converter.convert(it.value))
                }
            }
        }

        return EventResult(true)
    }

    override fun update(update: SqlUpdateClip<*, *>, eventData: EventResult?) {

    }

}