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
    var dbRefValues: List<JsonMap>
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


    override fun beforeUpdate(update: MongoBaseUpdateClip): EventResult {
        if (scopes.getLatest(MyOqlOrmScope.IgnoreCascadeUpdate) != null) {
            return EventResult(true, null)
        }


        var refs =
            MongoEntityCollector.refsMap.filter { MyUtil.getSmallCamelCase(it.refEntityClass.simpleName) == update.defEntityName }
        if (refs.any() == false) {
            return EventResult(true, null)
        }

        var list = mutableListOf<CascadeUpdateEventDataModel>()

        //update set 指定了其它表引用的冗余列。
        var updateSetFields = update.getChangedFieldData();


        //如果更新字段 和 引用字段无关，则跳过。
        var setCascadeColumns = updateSetFields.keys.intersect(refs.map { it.refNameFields }.Unwind().toSet());
        if (setCascadeColumns.any() == false) {
            //如果没有修改关联字段。直接退出。
            return EventResult(true, null)
        }
//        var masterIdFields = refs.map { it.refIdField }.toSet();


        var idValuesCache = mutableMapOf<String, Array<String>>()

        refs.forEach { ref ->
            var refNameFields = ref.refNameFields;
            if (updateSetFields.keys.intersect(refNameFields).any() == false) {
                return@forEach
            }

            var idValue = getIdValue(idValuesCache, ref, update)


            //判断新值，旧值是否相等
            val nameValueQuery = MongoBaseQueryClip(update.actualTableName)
            nameValueQuery.whereData.addAll(update.whereData)

            refNameFields.forEach {
                nameValueQuery.selectField(it)
            }

            val dbRefValues = nameValueQuery.toList(JsonMap::class.java);

            /**
             * 优化项： 如果只更新了一个字段。 单条，且值没有发生变化， 则跳过。
             */
            if (dbRefValues.size == 1 && refNameFields.size == 1) {
                var ff = refNameFields.first();
                if (dbRefValues.first().get(ff) == updateSetFields.getValue(ff)) {
                    return@forEach
                }
            }

            list.add(
                CascadeUpdateEventDataModel(
                    ref,
                    idValue,
                    dbRefValues
                )
            )
        }

        return EventResult(true, list)
    }

    private fun getIdValue(
        idValuesCache: MutableMap<String, Array<String>>,
        ref: DbEntityFieldRefData,
        update: MongoBaseUpdateClip
    ): Array<String> {

        var idValue = idValuesCache.getOrPut(ref.refIdField) {
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
            val IdValueQuery = MongoBaseQueryClip(update.actualTableName)
            IdValueQuery.whereData.addAll(update.whereData)
            IdValueQuery.selectField(ref.refIdField)

            return@getOrPut IdValueQuery.toList(String::class.java).toTypedArray()
        }
        return idValue
    }

    override fun update(update: MongoBaseUpdateClip, eventData: EventResult) {
        if (eventData.extData == null) {
            return;
        }

        var cascadeUpdates = eventData.extData as Collection<CascadeUpdateEventDataModel>
        if (cascadeUpdates.any() == false) return;

        var updateSetFields = update.getChangedFieldData();
        cascadeUpdates
            .filter { it.masterIdValues.any() }
            .forEach { cu ->
                val targetCollection = MyUtil.getSmallCamelCase(cu.ref.entityClass.simpleName)

                val update2 = MongoBaseUpdateClip(targetCollection)
                update2.whereData.putAll((MongoColumnName(cu.ref.field + "." + cu.ref.idField) match_in cu.masterIdValues).criteriaObject)

                if (cu.ref.fieldIsArray) {
                    cu.ref.refNameFields.forEach { column ->
                        var setV = updateSetFields.get(column)
                        if (setV == null) {
                            return@forEach
                        }
                        update2.setValue(cu.ref.field + ".$." + column, setV)
                    }
                } else {
                    cu.ref.refNameFields.forEach { column ->
                        var setV = updateSetFields.get(column)
                        if (setV == null) {
                            return@forEach
                        }
                        update2.setValue(cu.ref.field + "." + column, setV)
                    }
                }

                update2.exec();

                logger.Important(
                    "mongo级联更新${update2.affectRowCount}条记录,${update.actualTableName}.${cu.ref.field}(${cu.ref.idField})-->${targetCollection}(${cu.ref.refIdField}),${
                        cu.masterIdValues.joinToString(
                            ","
                        )
                    }"
                )
            }
    }

}