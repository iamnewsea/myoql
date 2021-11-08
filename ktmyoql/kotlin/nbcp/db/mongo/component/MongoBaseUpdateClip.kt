package nbcp.db.mongo

import com.mongodb.client.result.UpdateResult
import nbcp.comm.*
import nbcp.db.MyOqlOrmScope
import nbcp.db.db
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.CriteriaDefinition
import org.springframework.data.mongodb.core.query.Query
import java.time.LocalDateTime

open class MongoBaseUpdateClip(tableName: String) : MongoClipBase(tableName), IMongoWhereable {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    val whereData = mutableListOf<Criteria>()
//        private set


    /**保存 arrayFilters
     * https://docs.mongodb.com/manual/reference/method/db.collection.update/index.html#update-arrayfilters
     */
    protected val arrayFilters: MutableList<CriteriaDefinition> = mutableListOf()

    public val setData = LinkedHashMap<String, Any?>()
    protected val unsetData = mutableListOf<String>()
    protected val pushData = LinkedHashMap<String, Any>() //加
    protected val pullData = LinkedHashMap<String, Any>() //删
    protected val incData = LinkedHashMap<String, Int>() //


    //---------------------------------------------

    fun setValue(column: String, value: Any?) {
        this.setData.put(column, value);
    }

    fun getChangedFieldData(): Map<String, Any?> {
        var ret = mutableMapOf<String, Any?>()
        ret.putAll(this.setData);
        ret.putAll(this.unsetData.map { it to null })
        return ret;
    }


    /**
     * 更新条件不能为空。
     */
    open fun exec(): Int {
        if (whereData.size == 0) {
            throw RuntimeException("更新条件为空，不允许更新")
        }

        return execAll();
    }


    /**
     * 更新条件可以为空。
     */
    protected fun execAll(): Int {
        db.affectRowCount = -1;

        var settingResult = db.mongo.mongoEvents.onUpdating(this)
        if (settingResult.any { it.second.result == false }) {
            return 0;
        }

        var criteria = this.getMongoCriteria(*whereData.toTypedArray());

        var update = org.springframework.data.mongodb.core.query.Update();

        for (kv in setData) {
            var value = kv.value;
            if (value != null) {
                value = db.mongo.procSetDocumentData(kv.value!!)
                update = update.set(kv.key, value);
            } else {
                update = update.unset(kv.key);
            }
        }

        for (it in unsetData) {
            update = update.unset(it);
        }

        for (kv in pushData) {
            var value = db.mongo.procSetDocumentData(kv.value)
            update = update.push(kv.key, value);
        }


        for (kv in pullData) {
            var value = kv.value;
            if (value is Criteria) {
                update = update.pull(kv.key, value.criteriaObject);
            } else {
                var type = value::class.java;
                if (type.IsSimpleType() == false) {
                    throw RuntimeException("pull 必须是简单类型")
                }

                update = update.pull(kv.key, value);
            }
        }

        incData.forEach {
            if (it.value != 0) {
                update = update.inc(it.key, it.value);
            }
        }

        //如果没有要更新的列.
        if (update.updateObject.keys.size == 0) {
            return 0;
        }

//        var eventObject: MongoUpdateEventObject? = null;
//        if (whereCriteriaObject.keys.contains("_id")) {
//            var _id_value = whereCriteriaObject["_id"].toString();
//
//            if (_id_value.HasValue) {
//                eventObject = MongoUpdateEventObject(collectionClazz, _id_value, update.updateObject)
//            }
//        }


        this.arrayFilters.forEach {
            update.filterArray(it)
        }

        var ret = -1;
        var startAt = LocalDateTime.now()
        var error: Exception? = null
        var query = Query.query(criteria)
        var result: UpdateResult? = null;
        try {
            this.script = getUpdateScript(criteria,update)
            result = mongoTemplate.updateMulti(
                query,
                update,
                collectionName
            );

            this.executeTime = LocalDateTime.now() - startAt

            if (result.modifiedCount > 0) {
                usingScope(
                    arrayOf(
                        MyOqlOrmScope.IgnoreAffectRow,
                        MyOqlOrmScope.IgnoreExecuteTime,
                        MyOqlOrmScope.IgnoreUpdateAt
                    )
                ) {
                    settingResult.forEach {
                        it.first.update(this, it.second)
                    }
                }
            }

            ret = result.matchedCount.toInt();
            this.affectRowCount = ret
        } catch (e: Exception) {
            error = e;
            throw e;
        } finally {
            MongoLogger.logUpdate(error, collectionName, query, update, result)
//            logger.InfoError(ret < 0) {
//                """[update] ${this.collectionName}
//[where] ${criteria.criteriaObject.ToJson()}
//[set] ${update.updateObject.ToJson()}
//[result] ${ret}
//[耗时] ${db.executeTime}"""
//            }
        }


        return ret;
    }

    private fun getUpdateScript(where: Criteria,
                                update: org.springframework.data.mongodb.core.query.Update): String {
        var msgs = mutableListOf<String>()
        msgs.add("[update] " + this.collectionName);
        msgs.add("[where] " + where.criteriaObject.ToJson())
        msgs.add("[update] " + update.updateObject.ToJson())

        return msgs.joinToString(const.line_break)
    }

}