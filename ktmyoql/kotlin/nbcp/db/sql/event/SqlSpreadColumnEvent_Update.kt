//package nbcp.db.sql
//
//import nbcp.comm.AllFields
//import nbcp.comm.ToJson
//import nbcp.utils.*
//import nbcp.db.*
//import nbcp.db.sql.entity.s_dustbin
//import org.springframework.stereotype.Component
//import java.lang.reflect.Field
//import kotlin.reflect.full.createInstance
//
///**
// * 处理删除数据后转移到垃圾箱功能
// */
//@Component
//class SqlSpreadColumnEvent_Update : ISqlEntityUpdate {
//    override fun beforeUpdate(update: SqlUpdateClip<*, *>): DbEntityEventResult? {
//        var annotations = mutableListOf<Field>()
//        update.mainEntity.tableClass.AllFields.forEach {
//            var ann = it.getAnnotation(SqlSpreadColumn::class.java);
//            if (ann != null) {
//                annotations.add(it);
//            }
//        }
//
//        annotations.forEach { field ->
//            update.sets.forEach { set ->
//                if( set.key.name != field.name){
//                    return@forEach
//                }
//                var value = set.value;
//                if (value == null) {
//                    return@forEach
//                }
//
//                field.type.AllFields.forEach {
//                    update.sets.put(SqlColumnName(DbType.of(it.type), update.tableName, field.name + "_" + it.name), it.get(value))
//                }
//
//                update.sets.remove(set.key)
//            }
//        }
//
//
//        return DbEntityEventResult(true)
//    }
//
//    override fun update(update: SqlUpdateClip<*, *>, eventData: DbEntityEventResult?) {
//    }
//
//}