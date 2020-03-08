package nbcp.db.mongo

import nbcp.base.extend.NoAffectRowCount
import nbcp.base.extend.getStringValue
import nbcp.base.extend.using
import nbcp.base.utils.MyUtil
import nbcp.db.*
import nbcp.db.mongo.MongoDeleteClip
import nbcp.db.mongo.MongoEntityEvent
import nbcp.db.mongo.component.MongoBaseUpdateClip
import nbcp.db.mongo.entity.SysDustbin
import nbcp.db.mongo.entity.SysLog
import nbcp.db.mongo.table.MongoBaseGroup
import nbcp.db.mongo.toDocument
import org.bson.Document
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.query.BasicQuery
import org.springframework.stereotype.Component

/**
 * 处理 @DbEntityLogHistory
 */

@Component
class MongoLogHistoryUpdateEvent : IMongoEntityUpdate {
    override fun beforeUpdate(update: MongoBaseUpdateClip): DbEntityEventResult {
        var logs = MongoEntityEvent.logHistoryMap.filter { MyUtil.getSmallCamelCase(it.key.simpleName) == update.collectionName }
        if (logs.any() == false) {
            return DbEntityEventResult(true, null)
        }

        var fields = logs.values.first();


        //update set 指定了其它表引用的冗余列。
        var setData = update.getChangedFieldData();

        var settedField = setData.keys.intersect(fields.toList());
        if (settedField.any() == false) {
            return DbEntityEventResult(true, null)
        }

        //查询数据，把Id查出来。
        var query = MongoBaseQueryClip(update.collectionName)
        query.whereData.addAll(update.whereData)
        query.selectField("_id");

        setData.keys.forEach {
            query.selectField(it)
        }

        var list = query.toList(Document::class.java)
        return DbEntityEventResult(true, list)
    }

    override fun update(update: MongoBaseUpdateClip, eventData: DbEntityEventResult) {
        if (eventData.extData == null) {
            return;
        }

        var ret = eventData.extData as List<Document>
        if (ret.any() == false) return;


        //批量记录到日志
        var batchInsert = db.mor_base.sysLog.batchInsert()

        ret.forEach {

            var log = SysLog()
            log.module = "DbEntityLogHistory"
            log.type = "info"
            log.key = update.collectionName
            log.msg = "更新了关键字段值"
            log.data = it;

            batchInsert.add(log);
        }

        using(NoAffectRowCount()) {
            batchInsert.exec();
        }
    }

}