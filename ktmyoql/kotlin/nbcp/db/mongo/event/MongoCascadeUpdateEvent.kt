package nbcp.db.mongo.event;

import nbcp.comm.*
import nbcp.db.mongo.*;
import nbcp.utils.*
import nbcp.db.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * 同步处理，更新的实体，级联更新引用的冗余字段。
 */

data class CascadeUpdateEventDataModel(
    var ref: DbEntityFieldRefData,
    var masterIdValues: Array<String>,
    var masterNameValue: Any?
)

@Component
class MongoCascadeUpdateEvent : IMongoEntityUpdate {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }


    override fun beforeUpdate(update: MongoBaseUpdateClip): EventResult {
        if (scopes.getLatest(MyOqlOrmScope.IgnoreCascadeUpdate) != null) {
            return EventResult(true, null)
        }


        var refs =
            MongoEntityCollector.refsMap.filter { MyUtil.getSmallCamelCase(it.refEntityClass.simpleName) == update.collectionName }
        if (refs.any() == false) {
            return EventResult(true, null)
        }

        var list = mutableListOf<CascadeUpdateEventDataModel>()

        //update set 指定了其它表引用的冗余列。
        var updateSetFields = update.getChangedFieldData();

        var masterNameFields = refs.map { it.refNameField }.toSet();

        var setCascadeColumns = updateSetFields.keys.intersect(masterNameFields);
        if (setCascadeColumns.any() == false) {
            //如果没有修改关联字段。直接退出。
            return EventResult(true, null)
        }
//        var masterIdFields = refs.map { it.refIdField }.toSet();

        //如果按id更新。
        var whereMap = update.getMongoCriteria(*update.whereData.toTypedArray()).toDocument();

        var idValues = mutableMapOf<String, Array<String>>()

        refs.forEach { ref ->
            if (updateSetFields.containsKey(ref.refNameField) == false) {
                return@forEach
            }

            var idValue = idValues.getOrPut(ref.refIdField) {

                val refIdField = whereMap.keys.firstOrNull { key ->
                    if (key == ref.refIdField) {
                        return@firstOrNull true
                    }

                    if (key == "_id" && ref.refIdField == "id") {
                        return@firstOrNull true
                    }

                    if (key.endsWith("._id") && ref.refIdField.endsWith(".id") &&
                        key.Slice(0, -4) == ref.refIdField.Slice(0, -3)
                    ) {
                        return@firstOrNull true;
                    }

                    return@firstOrNull false
                }


                if (refIdField != null) {
                    return@getOrPut arrayOf(whereMap.getStringValue(refIdField).AsString())
                }

                //查询数据，把Id查出来。
                var query = MongoBaseQueryClip(update.collectionName)
                query.whereData.addAll(update.whereData)
                query.selectField(ref.refIdField)

                return@getOrPut query.toList(String::class.java).toTypedArray()
            }

            list.add(
                CascadeUpdateEventDataModel(
                    ref,
                    idValue,
                    updateSetFields.getValue(ref.refNameField)
                )
            )
        }

        return EventResult(true, list)
    }

    override fun update(update: MongoBaseUpdateClip, eventData: EventResult) {
        if (eventData.extData == null) {
            return;
        }

        var ret = eventData.extData as Collection<CascadeUpdateEventDataModel>
        if (ret.any() == false) return;


        ret.forEach { ref ->
            if (ref.masterIdValues.any()) {
                val targetCollection = MyUtil.getSmallCamelCase(ref.ref.entityClass.simpleName)
                val update2 = MongoBaseUpdateClip(targetCollection)
                update2.whereData.add(MongoColumnName(ref.ref.idField) match_in ref.masterIdValues.map {
                    getObjectIdValueTypeIfNeed(
                        it
                    )
                })
                update2.setValue(ref.ref.nameField, ref.masterNameValue)

                update2.exec();


                usingScope(LogLevelScope.info) {
                    logger.info("因为更新 ${update.collectionName}: ${ref.masterIdValues.joinToString(",")} + ${ref.masterNameValue} 而导致级联更新 ${targetCollection}：${ref.ref.idField} + ${ref.ref.nameField}")
                }
            }
        }
    }

}