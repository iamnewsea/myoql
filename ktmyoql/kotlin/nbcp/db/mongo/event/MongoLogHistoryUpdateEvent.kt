package nbcp.db.mongo.event;

import nbcp.db.mongo.*;
import nbcp.utils.*
import nbcp.db.*
import nbcp.db.mongo.entity.*
import org.bson.Document
import org.springframework.stereotype.Component

/**
 * 处理 @DbEntityLogHistory
 */

@Component
class MongoLogHistoryUpdateEvent : IMongoEntityUpdate {
    override fun beforeUpdate(update: MongoBaseUpdateClip): EventResult {
        var logs =
                MongoEntityCollector.logHistoryMap.filter { MyUtil.getSmallCamelCase(it.key.actualTableName) == update.actualTableName }
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
        query.whereData.putAll(update.whereData)
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
        val batchInsert = db.mor_base.sysLog.batchInsert()

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