package nbcp.db.mongo

import com.mongodb.BasicDBObject
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.repository.MongoRepository
import nbcp.base.extend.HasValue
import nbcp.base.extend.IsSimpleType
import nbcp.base.extend.ToJson
import nbcp.base.extend.*
import nbcp.base.utils.MyUtil
import nbcp.db.db
import nbcp.db.mongo.*
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

/**
 * Created by udi on 17-4-7.
 */

//根据Id，更新Mongo的一个键。

/**
 * MongoUpdate
 */
class MongoUpdateClip<M : MongoBaseEntity<out IMongoDocument>>(var moerEntity: M) : MongoClipBase(moerEntity.tableName), IMongoWhereable {
    companion object {
        private val logger by lazy {
            return@lazy LoggerFactory.getLogger(this::class.java)
        }
    }

    private var whereData = mutableListOf<Criteria>()
    private var setData = LinkedHashMap<String, Any?>()
    private var unsetData = mutableListOf<String>()
    private val pushData = LinkedHashMap<String, Any>() //加
    private val pullData = LinkedHashMap<String, Any>() //删
    private val incData = LinkedHashMap<String, Int>() //


    fun getSettedData(): Map<String, Any?> {
        return this.setData;
    }

    fun getWhereData(): List<Criteria> {
        return this.whereData;
    }

    fun where(whereData: Criteria): MongoUpdateClip<M> {
        this.whereData.add(whereData);
        return this;
    }

    fun where(where: (M) -> Criteria): MongoUpdateClip<M> {
        this.whereData.add(where(moerEntity));
        return this;
    }

    fun whereOr(vararg wheres: (M) -> Criteria): MongoUpdateClip<M> {
        return whereOr(*wheres.map { it(moerEntity) }.toTypedArray())
    }

    fun whereOr(vararg wheres: Criteria): MongoUpdateClip<M> {
        var where = Criteria();
        where.orOperator(*wheres)
        this.whereData.add(where);
        return this;
    }

    /**
     * 如果条件成立,则使用 where
     */
    fun whereIf(whereIf: Boolean, where: ((M) -> Criteria)): MongoUpdateClip<M> {
        if (whereIf == false) return this;

        this.whereData.add(where(moerEntity));
        return this;
    }

    /**
     * 如果条件成立,则使用 set
     */
    fun setIf(setIf: Boolean, valuePair: (M) -> Pair<MongoColumnName, Any?>): MongoUpdateClip<M> {
        if (setIf == false) return this;
        var v = valuePair(this.moerEntity)

        this.setData.put(v.first.toString(), v.second);
        return this;
    }


    fun set(key: String, value: Any?): MongoUpdateClip<M> {
        return set { MongoColumnName(key) to value }
    }

//    fun set(key: MongoColumnName, value: Any?): MongoUpdateClip<M> {
//        this.setData.put(key.toString(), value);
//        return this;
//    }
//    fun set(func: (M) -> MongoColumnName, value: Any?): MongoUpdateClip<M> {
//        var p = func(moerEntity);
//        this.setData.put(p.toString(), value);
//        return this;
//    }

    fun set(func: (M) -> Pair<MongoColumnName, Any?>): MongoUpdateClip<M> {
        var p = func(moerEntity);
        this.setData.put(p.first.toString(), p.second);
        return this;
    }

    //判断是否设置了某个值.
//    fun setted(key: String, value: Any): Boolean {
//        return this.setData.get(key) == value;
//    }

    fun unset(key: String): MongoUpdateClip<M> {
        this.unsetData.add(key);
        return this;
    }

    fun unset(keyFunc: (M) -> MongoColumnName): MongoUpdateClip<M> {
        this.unsetData.add(keyFunc(this.moerEntity).toString());
        return this;
    }

//    fun set(actionIf: () -> Pair<String, Any?>?): MongoUpdateClip<T> {
//        var setData = actionIf();
//
//        if (setData != null) {
//            this.setData.put(setData.first, setData.second);
//        }
//        return this;
//    }

    /**
     * 数据加法
     * .inc{ it.incField to 1 }
     */
    fun inc(incData: (M) -> Pair<MongoColumnName, Int>): MongoUpdateClip<M> {
        var kv = incData(this.moerEntity)
        this.incData.put(kv.first.toString(), kv.second);
        return this;
    }

    /**
     * 向数组中添加一条。
     * key:是实体的属性，内容是数组，如 roles。
     * pushItem是要插入实体的各个值。如： _id pair "ab" , name pair "def"
     */
    //    fun push(key: String, vararg pushItem: Pair<String, Any?>): MongoUpdateClip<T> {
//        this.pushData.put(key, LinkedHashMap<String, Any?>(pushItem.toMap()));
//        return this;
//    }

    /**
     * 向数组中添加一条。
     * key:是实体的属性，内容是数组，如 roles。
     * value是要插入实体值。如： UserRole
     */
    fun push(pair: (M) -> Pair<MongoColumnName, Any>): MongoUpdateClip<M> {
        var pairObject = pair(this.moerEntity);
        this.pushData.put(pairObject.first.toString(), pairObject.second);
        return this;
    }

    /**
     * 从数组中删除一条。
     * key:是实体的属性，内容是数组，如 roles。
     * pullWhere 是要删除实体的条件。如： _id pair "ab" , name pair "def"
     */
    fun pull(key: (M) -> MongoColumnName, vararg pullWhere: Criteria): MongoUpdateClip<M> {
        this.pullData.put(key(this.moerEntity).toString(), this.moerEntity.getMongoCriteria(*pullWhere));
        return this;
    }

    /**
     * 删除数组中的单个值.
     * @param pair  key=删除的数组列表达式， value=删除该列的值。
     */
    fun pull(pair: (M) -> Pair<MongoColumnName, String>): MongoUpdateClip<M> {
        var pairObject = pair(this.moerEntity);
        this.pullData.put(pairObject.first.toString(), pairObject.second);
        return this;
    }

    /**
     * 更新条件不能为空。
     */
    fun exec(): Int {
        if (whereData.size == 0) {
            throw Exception("更新条件为空，不允许更新")
            return 0;
        }

        if (this.setData.containsKey("updateAt") == false) {
            this.setData.put("updateAt", LocalDateTime.now())
        }
        return execAll();
    }


    /**
     * 更新条件可以为空。
     */
    fun execAll(): Int {

//        procMongo_IdColumn(whereData);

        var criteria = this.moerEntity.getMongoCriteria(*whereData.toTypedArray());

        var whereCriteriaObject = criteria.criteriaObject

//        for (wkv in whereData) {
//            criteria = criteria.and(wkv.first).`is`(wkv.second);
//        }

        var update = org.springframework.data.mongodb.core.query.Update();

        for (kv in setData) {
            if (kv.value != null) {
                update = update.set(kv.key, kv.value!!);
            } else {
                update = update.unset(kv.key);
            }
        }

        for (it in unsetData) {
            update = update.unset(it);
        }

        for (kv in pushData) {
            update = update.push(kv.key, kv.value);
        }


        for (kv in pullData) {
//            procMongo_IdColumn(kv.value)
            var value = kv.value;
            if (value is Criteria) {
                update = update.pull(kv.key, value.criteriaObject);
            } else {
                var type = value::class.java;
                if (type.IsSimpleType() == false) {
                    throw Exception("pull 必须是简单类型")
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
            db.affectRowCount = 0;
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


        var settingResult = db.mongoEvents.onUpdating(this)
        if (settingResult.result == false) {
            return 0;
        }

        var ret = 0;
        try {
            var result = mongoTemplate.updateMulti(
                    Query.query(criteria),
                    update,
                    collectionName);

            if (result.modifiedCount > 0) {
                db.mongoEvents.onUpdated(this, settingResult.extData)
            }

            ret = result.matchedCount.toInt();
            db.affectRowCount = ret
        } catch (e: Exception) {
            ret = -1;
            throw e;
        } finally {
            var msg = "update:[" + this.collectionName + "] " + whereCriteriaObject.toJson() + " ,result:" + ret;
            if (ret < 0) {
                logger.error(msg)
            } else {
                logger.info(msg)
            }
        }


        return ret;
    }
}

