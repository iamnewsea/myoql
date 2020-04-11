//package nbcp.db.es
//
//import nbcp.base.extend.getStringValue
//import nbcp.base.utils.MyUtil
//import nbcp.db.DbEntityEventResult
//import nbcp.db.DbEntityFieldRefData
//import org.springframework.stereotype.Component
//
///**
// * 同步处理，更新的实体，级联更新引用的冗余字段。
// */
//
//data class CascadeUpdateEventDataModel(
//        var ref: DbEntityFieldRefData,
//        var masterIdValues: Array<String>,
//        var masterNameValue: String
//)
//
//@Component
//class EsCascadeUpdateEvent : IEsEntityUpdate {
//    override fun beforeUpdate(update: EsBaseUpdateClip): DbEntityEventResult {
//        var refs = EsEntityEvent.refsMap.filter { MyUtil.getSmallCamelCase(it.masterEntityClass.simpleName) == update.collectionName }
//        if (refs.any() == false) {
//            return DbEntityEventResult(true, null)
//        }
//
//        var list = mutableListOf<CascadeUpdateEventDataModel>()
//
//        //update set 指定了其它表引用的冗余列。
//        var setData = update.getChangedFieldData();
//
//        var masterNameFields = refs.map { it.masterNameField }.toSet();
//
//        var setCascadeColumns = setData.keys.intersect(masterNameFields);
//        if (setCascadeColumns.any() == false) {
//            return DbEntityEventResult(true, null)
//        }
//        var masterIdFields = refs.map { it.masterIdField }.toSet();
//
//        //如果按id更新。
//        var whereMap = update.getEsCriteria(*update.whereData.toTypedArray()).toDocument();
//
//        var idValues = mutableMapOf<String, Array<String>>()
//
//        refs.forEach { ref ->
//            var idValue = idValues.getOrPut(ref.masterIdField) {
//                if (whereMap.keys.contains(ref.masterIdField)) {
//
//                    return@getOrPut arrayOf(whereMap.getStringValue(ref.masterIdField))
//                } else {
//                    //查询数据，把Id查出来。
//                    var query = EsBaseQueryClip(update.collectionName)
//                    query.whereData.addAll(update.whereData)
//                    query.selectField(ref.masterIdField)
//
//                    return@getOrPut query.toList(String::class.java).toTypedArray()
//                }
//            }
//
//            list.add(CascadeUpdateEventDataModel(ref,
//                    idValue,
//                    setData.getStringValue(ref.masterNameField)
//            ))
//        }
//
//
//        return DbEntityEventResult(true, list)
//    }
//
//    override fun update(update: EsBaseUpdateClip, eventData: DbEntityEventResult) {
//        if (eventData.extData == null) {
//            return;
//        }
//
//        var ret = eventData.extData as List<CascadeUpdateEventDataModel>
//        if (ret.any() == false) return;
//
//
//        ret.forEach { ref ->
//            var update2 = EsBaseUpdateClip(MyUtil.getSmallCamelCase(ref.ref.entityClass.simpleName))
//            update2.whereData.add(EsColumnName(ref.ref.idField) match_in ref.masterIdValues)
//            update2.setValue(ref.ref.nameField, ref.masterNameValue)
//            update2.exec();
//        }
//    }
//
//}