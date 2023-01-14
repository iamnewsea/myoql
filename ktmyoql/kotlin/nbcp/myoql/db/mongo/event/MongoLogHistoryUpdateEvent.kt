package nbcp.myoql.db.mongo.event;

import nbcp.base.utils.MyUtil
import nbcp.base.utils.StringUtil
import nbcp.myoql.db.comm.EventResult
import nbcp.myoql.db.db
import nbcp.myoql.db.mongo.MongoBaseQueryClip
import nbcp.myoql.db.mongo.MongoEntityCollector
import nbcp.myoql.db.mongo.batchInsert
import nbcp.myoql.db.mongo.component.MongoBaseUpdateClip
import nbcp.myoql.db.mongo.entity.SysLog
import org.bson.Document
import org.springframework.stereotype.Component

/**
 * 处理 @DbEntityLogHistory
 */

@Component
class MongoLogHistoryUpdateEvent : IMongoEntityUpdate {
    override fun beforeUpdate(update: MongoBaseUpdateClip): EventResult {
        var logs =
                MongoEntityCollector.logHistoryMap.filter { StringUtil.getSmallCamelCase(it.key.actualTableName) == update.actualTableName }
        if (logs.any() == false) {
            return EventResult(true, null)
        }

        var fields = logs.values.first();


        //update set 指定了其它表引用的冗余列。
        var setData = update.getChangedFieldData();

        var settedField = setData.keys.intersect(fields.toList());
        if (settedField.any() == false) {
            return EventResult(true, null)
        }

        //查询数据，把Id查出来。
        var query = MongoBaseQueryClip(update.actualTableName)
        query.whereData.addAll(update.whereData)
        query.selectField("_id");

        setData.keys.forEach {
            query.selectField(it)
        }

        var list = query.toList(Document::class.java)
        return EventResult(true, list)
    }

    override fun update(update: MongoBaseUpdateClip, eventData: EventResult) {
        if (eventData.extData == null) {
            return;
        }

        var ret = eventData.extData as List<Document>
        if (ret.any() == false) return;


        //批量记录到日志
        val batchInsert = db.morBase.sysLog.batchInsert()

        ret.forEach {

            val log = SysLog()
            log.module = "DbEntityLogHistory"
            log.level = "info"
            log.tags = mutableListOf(update.actualTableName)
            log.msg = "更新了关键字段值"
            log.data = it;

            batchInsert.add(log);
        }

        batchInsert.exec();
    }
}