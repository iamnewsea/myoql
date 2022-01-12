package nbcp.db.mongo.event;

import nbcp.comm.*
import nbcp.db.mongo.*;
import nbcp.utils.*
import nbcp.db.*
import org.bson.Document
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

/**
 * 如果是级联更新数组，用下面的写法
 * DbEntityFieldRef("corp.id","corp.$.name","SysCorporation","id","name")
 */
@Component
class MongoCascadeUpdateEvent : IMongoEntityUpdate {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }


    override fun beforeUpdate(update: MongoBaseUpdateClip, chain: EventChain): EventResult {
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


        var idValues = mutableMapOf<String, Array<String>>()

        refs.forEach { ref ->
            if (updateSetFields.containsKey(ref.refNameField) == false) {
                return@forEach
            }

            var idValue = getIdValue(idValues, ref, update)

            val masterNameValue = updateSetFields.getValue(ref.refNameField);

            //判断新值，旧值是否相等
            val nameValueQuery = MongoBaseQueryClip(update.collectionName)
            nameValueQuery.whereData.putAll(update.whereData)
            nameValueQuery.selectField(ref.refNameField)

            val dbNameValues = nameValueQuery.toList(String::class.java).toSet();

            if (dbNameValues.size == 1 && dbNameValues.first() == masterNameValue) {
                return@forEach
            }

            list.add(
                CascadeUpdateEventDataModel(
                    ref,
                    idValue,
                    masterNameValue
                )
            )
        }

        return EventResult(true, list)
    }

    private fun getIdValue(
        idValues: MutableMap<String, Array<String>>,
        ref: DbEntityFieldRefData,
        update: MongoBaseUpdateClip
    ): Array<String> {

        var idValue = idValues.getOrPut(ref.refIdField) {
            //如果按id更新。
            var whereMap = db.mongo.getMergedMongoCriteria(update.whereData).toDocument();

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
            val IdValueQuery = MongoBaseQueryClip(update.collectionName)
            IdValueQuery.whereData.putAll(update.whereData)
            IdValueQuery.selectField(ref.refIdField)

            return@getOrPut IdValueQuery.toList(String::class.java).toTypedArray()
        }
        return idValue
    }

    override fun update(update: MongoBaseUpdateClip, chain: EventChain, eventData: EventResult) {
        if (eventData.extData == null) {
            return;
        }

        var cascadeUpdates = eventData.extData as Collection<CascadeUpdateEventDataModel>
        if (cascadeUpdates.any() == false) return;


        cascadeUpdates
            .filter { it.masterIdValues.any() }
            .forEach { ref ->
                val targetCollection = MyUtil.getSmallCamelCase(ref.ref.entityClass.simpleName)

                val update2 = MongoBaseUpdateClip(targetCollection)
                update2.whereData.putAll((MongoColumnName(ref.ref.idField) match_in ref.masterIdValues).criteriaObject)
                update2.setValue(ref.ref.nameField, ref.masterNameValue)
                update2.exec();

                logger.Important(
                    "mongo级联更新${update2.affectRowCount}条记录,${update.collectionName}-->${targetCollection},${
                        ref.masterIdValues.joinToString(
                            ","
                        )
                    }-->(${ref.ref.idField},${ref.ref.nameField})"
                )
            }
    }

}