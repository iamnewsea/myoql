package nbcp.db.mongo.event;

import nbcp.db.mongo.*;
import nbcp.comm.AsString
import nbcp.comm.LogScope
import nbcp.comm.getStringValue
import nbcp.comm.usingScope
import nbcp.utils.*
import nbcp.db.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.Serializable

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
        var refs =
            MongoEntityEvent.refsMap.filter { MyUtil.getSmallCamelCase(it.masterEntityClass.simpleName) == update.collectionName }
        if (refs.any() == false) {
            return EventResult(true, null)
        }

        var list = mutableListOf<CascadeUpdateEventDataModel>()

        //update set 指定了其它表引用的冗余列。
        var setData = update.getChangedFieldData();

        var masterNameFields = refs.map { it.masterNameField }.toSet();

        var setCascadeColumns = setData.keys.intersect(masterNameFields);
        if (setCascadeColumns.any() == false) {
            return EventResult(true, null)
        }
        var masterIdFields = refs.map { it.masterIdField }.toSet();

        //如果按id更新。
        var whereMap = update.getMongoCriteria(*update.whereData.toTypedArray()).toDocument();

        var idValues = mutableMapOf<String, Array<String>>()

        refs.forEach { ref ->
            var idValue = idValues.getOrPut(ref.masterIdField) {
                if (whereMap.keys.contains(ref.masterIdField)) {

                    return@getOrPut arrayOf(whereMap.getStringValue(ref.masterIdField).AsString())
                } else {
                    //查询数据，把Id查出来。
                    var query = MongoBaseQueryClip(update.collectionName)
                    query.whereData.addAll(update.whereData)
                    query.selectField(ref.masterIdField)

                    return@getOrPut query.toList(String::class.java).toTypedArray()
                }
            }

            list.add(
                CascadeUpdateEventDataModel(
                    ref,
                    idValue,
                    setData.getValue(ref.masterNameField)
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
                var targetCollection = MyUtil.getSmallCamelCase(ref.ref.entityClass.simpleName)
                var update2 = MongoBaseUpdateClip(targetCollection)
                update2.whereData.add(MongoColumnName(ref.ref.idField) match_in ref.masterIdValues.map {
                    getObjectIdValueTypeIfNeed(
                        it
                    )
                })
                update2.setValue(ref.ref.nameField, ref.masterNameValue)
                update2.exec();

                usingScope(LogScope.info) {
                    logger.info("因为更新 ${update.collectionName}: ${ref.masterIdValues.joinToString(",")} + ${ref.masterNameValue} 而导致级联更新 ${targetCollection}：${ref.ref.idField} + ${ref.ref.nameField}")
                }
            }
        }
    }

}