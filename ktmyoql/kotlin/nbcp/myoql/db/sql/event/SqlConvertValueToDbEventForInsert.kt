//package nbcp.db.sql.event;
//
//import nbcp.myoql.db.sql.*;
//import nbcp.comm.AllFields
//import nbcp.base.utils.*
//import nbcp.myoql.db.*
//import org.springframework.stereotype.Component
//import java.lang.reflect.Field
//import kotlin.reflect.KClass
//import kotlin.reflect.full.createInstance
//
///**
// *
// */
//@Component
//class SqlConvertValueToDbEventForInsert : ISqlEntityInsert {
//    override fun beforeInsert(insert: SqlInsertClip<*, *>): EventResult? {
//
//        var annotations = mutableMapOf<Field, Array<out KClass<out IConverter>>>()
//        insert.mainEntity.tableClass.AllFields.forEach {
//            var ann = it.getAnnotation(ConverterValueToDb::class.java);
//            if (ann != null) {
//                annotations.put(it, ann.value);
//            }
//        }
//
//        annotations.forEach { field, convertersType ->
//            val converters = convertersType.map { it.createInstance() }
//            val values = mutableListOf<Any?>()
//            insert.entities.forEach {
//                var convertedValue:Any? = it.get(field.name);
//                converters.forEach{
//                    convertedValue = it.convert(field,it);
//                }
//                values.add(convertedValue);
//                it.set(field.name, convertedValue);
//            }
//
//            insert.ori_entities.forEachIndexed { index, entity ->
//                MyUtil.setPrivatePropertyValue(entity, field.name, values[index])
//            }
//        }
//
//
//        return EventResult(true)
//    }
//
//    override fun insert(insert: SqlInsertClip<*, *>, eventData: EventResult?) {
//
//    }
//}