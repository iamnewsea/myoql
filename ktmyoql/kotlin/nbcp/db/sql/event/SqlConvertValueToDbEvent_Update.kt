package nbcp.db.sql.event;

import nbcp.db.sql.*;
import nbcp.comm.AllFields
import nbcp.db.*
import org.springframework.stereotype.Component
import java.lang.reflect.Field
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

/**
 *
 */
@Component
class SqlConvertValueToDbEvent_Update : ISqlEntityUpdate {
    override fun beforeUpdate(update: SqlUpdateClip<*>): EventResult? {
        var annotations = mutableMapOf<Field, Array<out KClass<out IConverter>>>()
        update.mainEntity.tableClass.AllFields.forEach {
            var ann = it.getAnnotation(ConverterValueToDb::class.java);
            if (ann != null) {
                annotations.put(it, ann.value);
            }
        }

        annotations.forEach { field, convertersType ->
            val converters = convertersType.map { it.createInstance() }
            update.sets.forEach {
                if (it.key.name == field.name) {

                    var convertedValue: Any? = it.value;
                    converters.forEach {
                        convertedValue = it.convert(field, it);
                    }

                    update.sets.put(it.key, convertedValue)
                }
            }
        }

        return EventResult(true)
    }

    override fun update(update: SqlUpdateClip<*>, eventData: EventResult?) {

    }

}