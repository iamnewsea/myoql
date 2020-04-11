package nbcp.db.mongo

import nbcp.base.extend.*
import nbcp.base.utils.RecursionUtil
import nbcp.comm.minus
import nbcp.db.db
import nbcp.db.mongo.IMongoWhereable
import nbcp.db.mongo.MongoClipBase
import org.bson.Document
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.CriteriaDefinition
import org.springframework.data.mongodb.core.query.Query
import java.time.LocalDateTime

open class MongoBaseUpdateClip(tableName: String) : MongoClipBase(tableName), IMongoWhereable {
    companion object {
        private val logger by lazy {
            return@lazy LoggerFactory.getLogger(this::class.java)
        }
    }

    val whereData = mutableListOf<Criteria>()
//        private set


    /**保存 arrayFilters
     * https://docs.mongodb.com/manual/reference/method/db.collection.update/index.html#update-arrayfilters
     */
    protected val arrayFilters: MutableList<CriteriaDefinition> = mutableListOf()

    protected val setData = LinkedHashMap<String, Any?>()
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
            return 0;
        }

        return execAll();
    }


    /**
     * 更新条件可以为空。
     */
    protected fun execAll(): Int {
        db.affectRowCount = -1;

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

        var settingResult = db.mongo.mongoEvents.onUpdating(this)
        if (settingResult.any { it.second.result == false }) {
            return 0;
        }

        var ret = 0;
        var startAt = LocalDateTime.now()
        try {
            var result = mongoTemplate.updateMulti(
                    Query.query(criteria),
                    update,
                    collectionName);

            db.executeTime = LocalDateTime.now() - startAt

            if (result.modifiedCount > 0) {
                using(OrmLogScope.IgnoreAffectRow) {
                    using(OrmLogScope.IgnoreExecuteTime) {
                        settingResult.forEach {
                            it.first.update(this, it.second)
                        }
                    }
                }
            }

            ret = result.matchedCount.toInt();
            db.affectRowCount = ret
        } catch (e: Exception) {
            ret = -1;
            throw e;
        } finally {
            logger.InfoError(ret < 0) {
                """[update] ${this.collectionName}
[where] ${criteria.criteriaObject.ToJson()}
[set] ${update.updateObject.ToJson()}
[result] ${ret}
[耗时] ${db.executeTime}"""
            }
        }


        return ret;
    }

}