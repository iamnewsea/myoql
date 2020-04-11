//package nbcp.db.es
//
//import nbcp.db.DbEntityEventResult
//import nbcp.db.db
//import nbcp.db.es.entity.*
//import org.springframework.stereotype.Component
//
//
///**
// * 同步处理，删除的数据转移到垃圾箱
// */
//@Component
//class EsDustbinEvent : IEsEntityDelete {
//    override fun beforeDelete(delete: EsDeleteClip<*>): DbEntityEventResult {
//        var contains = EsEntityEvent.dustbinEntitys.contains(delete.moerEntity.entityClass)
//        if (contains == false) {
//            return DbEntityEventResult(true, null);
//        }
//
//        //找出数据
//        var where = delete.getEsCriteria(*delete.whereData.toTypedArray());
//        var query = BasicQuery(where.toDocument())
//        var cursor = delete.esTemplate.find(query, Document::class.java, delete.collectionName)
//        return DbEntityEventResult(true, cursor)
//    }
//
//    override fun delete(delete: EsDeleteClip<*>, eventData: DbEntityEventResult) {
//        var data = eventData.extData
//        if (data == null) return
//
//        var dustbin = SysDustbin()
//        dustbin.id = ObjectId().toString()
//        dustbin.table = delete.collectionName
//        dustbin.data = data;
//        db.mor_base.sysDustbin.doInsert(dustbin)
//    }
//}