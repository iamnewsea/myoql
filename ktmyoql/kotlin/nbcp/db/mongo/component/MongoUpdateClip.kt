package nbcp.db.mongo

import nbcp.comm.Slice
import nbcp.comm.minus
import nbcp.comm.usingScope
import nbcp.db.DbIncData
import nbcp.db.MyOqlOrmScope
import nbcp.db.db
import org.bson.Document
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import java.io.Serializable
import java.time.LocalDateTime

/**
 * Created by udi on 17-4-7.
 */

//根据Id，更新Mongo的一个键。

/**
 * MongoUpdate
 */
class MongoUpdateClip<M : MongoBaseMetaCollection<out E>, E : Any>(var moerEntity: M) :
        MongoBaseUpdateClip(moerEntity.tableName) {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    fun where(whereData: Criteria): MongoUpdateClip<M, E> {
        this.whereData.add(whereData);
        return this;
    }

    fun removeWhereColumn(where: (M) -> MongoColumnName): MongoUpdateClip<M, E> {
        var item = where(moerEntity).toString()
        var items = mutableListOf(item);
        if (item == "id") {
            items.add("_id")
        } else if (item.endsWith(".id")) {
            items.add(item.Slice(0, -3) + "._id")
        }

        items.forEach { key->
            this.setData.remove(key);
        }
        return this;
    }

    fun where(where: (M) -> Criteria): MongoUpdateClip<M, E> {
        this.whereData.add(where(moerEntity));
        return this;
    }

    fun whereOr(vararg wheres: (M) -> Criteria): MongoUpdateClip<M, E> {
        return whereOr(*wheres.map { it(moerEntity) }.toTypedArray())
    }

    fun whereOr(vararg wheres: Criteria): MongoUpdateClip<M, E> {
        if (wheres.any() == false) return this;
        var where = Criteria();
        where.orOperator(*wheres)
        this.whereData.add(where);
        return this;
    }

    /**
     * 如果条件成立,则使用 where
     */
    fun whereIf(whereIf: Boolean, where: ((M) -> Criteria)): MongoUpdateClip<M, E> {
        if (whereIf == false) return this;

        this.whereData.add(where(moerEntity));
        return this;
    }

    /**
     * 如果条件成立,则使用 set
     */
    fun setIf(setIf: Boolean, valuePair: (M) -> Pair<MongoColumnName, Any?>): MongoUpdateClip<M, E> {
        if (setIf == false) return this;
        var v = valuePair(this.moerEntity)

        this.setData.put(v.first.toString(), v.second);
        return this;
    }


    fun set(key: String, value: Any?): MongoUpdateClip<M, E> {
        return set { MongoColumnName(key) to value }
    }

    fun set(func: (M) -> Pair<MongoColumnName, Any?>): MongoUpdateClip<M, E> {
        var p = func(moerEntity);
        this.setData.put(p.first.toString(), p.second);
        return this;
    }

    fun unset(key: String): MongoUpdateClip<M, E> {
        this.unsetColumns.add(key);
        return this;
    }

    fun unset(keyFunc: (M) -> MongoColumnName): MongoUpdateClip<M, E> {
        this.unsetColumns.add(keyFunc(this.moerEntity).toString());
        return this;
    }

    /**
     * 数据加法
     * .inc{ it.incField to 1 }
     */
    fun inc(incData: (M) -> DbIncData): MongoUpdateClip<M, E> {
        var kv = incData(this.moerEntity)
        this.incData.put(kv.column, kv.incValue);
        this.removeWhereColumn { MongoColumnName(kv.column) }
        return this;
    }

    /**
     * 向数组中添加一条。
     * key:是实体的属性，内容是数组，如 roles。
     * value是要插入实体值。如： UserRole
     */
    fun push(pair: (M) -> Pair<MongoColumnName, Serializable>): MongoUpdateClip<M, E> {
        var pairObject = pair(this.moerEntity);
        this.pushData.put(pairObject.first.toString(), pairObject.second);
        return this;
    }

    /**
     * 从数组中删除一条。
     * key:是实体的属性，内容是数组，如 roles。
     * pullWhere 是要删除实体的条件。
     * 如：
     * .pull( it.menus, "id" match "ab")
     * .pull( it.roles, "id" match "def")
     * ==>
     * { $pull: { "menus":{"id:"ab"} , "roles":{"id:"def"} } }
     *
     * @param key , 移除的数组列。
     * @param pullWhere , where条件的列，是从 @key 字段开始的列。
     */
    fun pull(key: (M) -> MongoColumnName, pullWhere: Criteria): MongoUpdateClip<M, E> {
        this.pullData.put(key(this.moerEntity).toString(), pullWhere);
        return this;
    }

    /**
     * 删除数组中的单个值.
     * @param pair  key=删除的数组列表达式， value=删除该列的值。
     * 如：
     * .pull( it.menus  to "1")
     * ==>
     * { $pull :{ "menus": "1" } }
     */
    fun pull(pair: (M) -> Pair<MongoColumnName, String>): MongoUpdateClip<M, E> {
        var pairObject = pair(this.moerEntity);
        this.pullData.put(pairObject.first.toString(), pairObject.second);
        return this;
    }

    /**
     *@param define: 更新的参数，应该使用 Where 表达式子类 Criteria 传递
     */
    fun arrayFilter(define: Criteria): MongoUpdateClip<M, E> {
        this.arrayFilters.add(define)
        return this;
    }

    fun saveAndReturnNew(mapFunc: ((Document) -> Unit)? = null): E? {
        return saveAndReturnNew(this.moerEntity.entityClass, mapFunc)
    }

    /**
     * 执行更新并返回更新后的数据（适用于更新一条的情况）
     */
    fun <T> saveAndReturnNew(clazz: Class<T>, mapFunc: ((Document) -> Unit)? = null): T? {
        db.affectRowCount = -1;

        var settingResult = db.mongo.mongoEvents.onUpdating(this)
        if (settingResult.any { it.result.result == false }) {
            return null;
        }

        var criteria = db.mongo.getMergedMongoCriteria(*whereData.toTypedArray());

        var update = getUpdateSetSect();

        //如果没有要更新的列.
        if (update.updateObject.keys.size == 0) {
            logger.warn("没有要更新的列，忽略更新!")
            return null;
        }

        var ret = -1;
        var startAt = LocalDateTime.now()
        var error: Exception? = null
        var query = Query.query(criteria)
        var resultDocument: Document? = null;
        var result: T? = null;

        var updateOption = FindAndModifyOptions();
        updateOption.returnNew(true)
        updateOption.upsert(true);
        try {
            this.script = getUpdateScript(criteria, update)
            resultDocument = mongoTemplate.findAndModify(
                    query,
                    update,
                    updateOption,
                    Document::class.java,
                    actualTableName
            );

            this.executeTime = LocalDateTime.now() - startAt

            if (resultDocument != null) {

                result = db.mongo.proc_mongo_doc_to_entity(resultDocument, clazz, "", mapFunc)


                usingScope(
                        arrayOf(
                                MyOqlOrmScope.IgnoreAffectRow,
                                MyOqlOrmScope.IgnoreExecuteTime,
                                MyOqlOrmScope.IgnoreUpdateAt
                        )
                ) {
                    settingResult.forEach {
                        it.event.update(this, it.chain, it.result)
                    }
                }
            }

            if (resultDocument != null) {
                ret = 1
            } else {
                ret = 0;
            }
            this.affectRowCount = ret
        } catch (e: Exception) {
            error = e;
            throw e;
        } finally {
            MongoLogger.logUpdate(error, actualTableName, query, update, null)
        }

        return result;
    }
}

