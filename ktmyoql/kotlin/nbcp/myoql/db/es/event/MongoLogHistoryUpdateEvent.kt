//package nbcp.db.es
//
//import nbcp.base.utils.*
//import nbcp.myoql.db.DbEntityEventResult
//import nbcp.myoql.db.db
//import nbcp.myoql.db.es.*
//import nbcp.myoql.db.mongo.entity.*
//import org.springframework.stereotype.Component
//
///**
// * 处理 @DbEntityLogHistory
// */
//
//@Component
//class EsLogHistoryUpdateEvent : IEsEntityUpdate {
//    override fun beforeUpdate(update: EsBaseUpdateClip): DbEntityEventResult {
//        var logs = EsEntityEvent.logHistoryMap.filter { StringUtil.getSmallCamelCase(it.key.simpleName) == update.collectionName }
//        if (logs.any() == false) {
//            return DbEntityEventResult(true, null)
//        }
//
//        var fields = logs.values.first();
//
//
//        //update set 指定了其它表引用的冗余列。
//        var setData = update.getChangedFieldData();
//
//        var settedField = setData.keys.intersect(fields.toList());
//        if (settedField.any() == false) {
//            return DbEntityEventResult(true, null)
//        }
//
//        //查询数据，把Id查出来。
//        var query = EsBaseQueryClip(update.collectionName)
//        query.whereData.addAll(update.whereData)
//        query.selectField("_id");
//
//        setData.keys.forEach {
//            query.selectField(it)
//        }
//
//        var list = query.toList(Document::class.java)
//        return DbEntityEventResult(true, list)
//    }
//
//    override fun update(update: EsBaseUpdateClip, eventData: DbEntityEventResult) {
//        if (eventData.extData == null) {
//            return;
//        }
//
//        var ret = eventData.extData as List<Document>
//        if (ret.any() == false) return;
//
//
//        //批量记录到日志
//        var batchInsert = db.mor_base.sysLog.batchInsert()
//
//        ret.forEach {
//
//            var log = SysLog()
//            log.module = "DbEntityLogHistory"
//            log.type = "info"
//            log.key = update.collectionName
//            log.msg = "更新了关键字段值"
//            log.data = it;
//
//            batchInsert.add(log);
//        }
//
//
//                batchInsert.exec();
//
//    }
//
//}