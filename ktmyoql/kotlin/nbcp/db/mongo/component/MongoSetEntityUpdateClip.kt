package nbcp.db.mongo


import nbcp.comm.*
import nbcp.db.MyOqlOrmScope
import nbcp.utils.*
import nbcp.db.db
import org.bson.Document
import org.bson.types.ObjectId
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
 * 不会更新 id
 */
class MongoSetEntityUpdateClip<M : MongoBaseMetaCollection<out E>, E:Any >(
    var moerEntity: M,
    var entity: E
) : MongoClipBase(moerEntity.tableName) {
    val whereData = mutableListOf<Criteria>()
    val setData = LinkedHashMap<String, Any?>()

    private var whereColumns = mutableSetOf<String>()
    private var setColumns = mutableSetOf<String>()
    private var unsetColumns = mutableSetOf<String>("createAt")

    fun withColumn(setFunc: (M) -> MongoColumnName): MongoSetEntityUpdateClip<M, E> {
        this.setColumns.add(setFunc(this.moerEntity).toString())
        return this;
    }

    fun withColumns(vararg column: String): MongoSetEntityUpdateClip<M, E> {
        this.setColumns.addAll(column)
        return this;
    }

    fun withoutColumn(unsetFunc: (M) -> MongoColumnName): MongoSetEntityUpdateClip<M, E> {
        this.unsetColumns.add(unsetFunc(this.moerEntity).toString())
        return this;
    }

    fun whereColumn(whereFunc: (M) -> MongoColumnName): MongoSetEntityUpdateClip<M, E> {
        this.whereColumns.add(whereFunc(this.moerEntity).toString())
        return this;
    }

//    /**
//     * 不应该依赖客户端，不应该使用这个方法
//     */
//    fun withRequestParams(keys: Set<String>): MongoSetEntityUpdateClip<M> {
//
//        return this
//    }

    //额外设置
    fun set(setItemAction: (M) -> Pair<MongoColumnName, Any?>): MongoSetEntityUpdateClip<M, E> {
        var setItem = setItemAction(this.moerEntity);
        this.setData.set(setItem.first.toString(), setItem.second);
        return this;
    }

    fun where(where: (M) -> Criteria): MongoSetEntityUpdateClip<M, E> {
        var c = where(moerEntity);
        this.whereData.add(c);

        if (c.key == "_id") {
            this.whereColumns.add("id")
        } else {
            this.whereColumns.add(c.key);
        }
        return this;
    }

    fun prepareUpdate(): MongoUpdateClip<M> {
        if (whereColumns.any() == false) {
            whereColumns.add("id")
        }

        var whereData2 = LinkedHashMap<String, Any?>()
        var setData2 = LinkedHashMap<String, Any?>()

        var update = MongoUpdateClip(this.moerEntity)

        if (this.entity is Map<*, *>) {
            //map 可能是  _id , 也可能是 id
            val map = this.entity as MutableMap<*, *>

            map.keys.map { it.toString() }.forEach {
                var findKey = it;
                val value = map.get(it);

                if (whereColumns.contains(findKey)) {
                    if (findKey == "_id") {
                        findKey = "id"
                    }
                    whereData2.put(findKey, value)
                    return@forEach
                }

                if (findKey == "id" || findKey == "_id") {
                    return@forEach
                }

                if (setColumns.any() && (setColumns.contains(findKey) == false)) {
                    return@forEach;
                }

                if (unsetColumns.contains(findKey)) {
                    return@forEach
                }

                setData2.put(findKey, value)
            }
        } else {
            this.entity::class.java.AllFields.forEach {
                var findKey = it.name;

                if (whereColumns.contains(findKey)) {
                    var value = MyUtil.getPrivatePropertyValue(this.entity, findKey);
                    whereData2.put(findKey, value)
                    return@forEach
                }

                if (findKey == "id") {
                    return@forEach
                }

                if (setColumns.any() && (setColumns.contains(findKey) == false)) {
                    return@forEach;
                }

                if (unsetColumns.contains(findKey)) {
                    return@forEach
                }

                var value = MyUtil.getPrivatePropertyValue(this.entity, findKey);
                setData2.put(findKey, value)
            }
        }

        var setKeys = this.whereData
            .map { it.toDocument().keys.toTypedArray() }
            .Unwind()
            .map { if (it == "_id") return@map "id" else return@map it };

        whereData2.forEach { key, value ->
            if (setKeys.contains(key)) {
                return@forEach
            }

            update.where(key match value);
        }
        this.whereData.forEach {
            update.where(it)
        }

        setData2.putAll(this.setData)

        setData2.forEach { key, value ->
            update.set(key, value);
        }

        return update;
    }

    /**
     * 执行更新, == exec ,语义清晰
     */
    fun execUpdate(): Int {
        return this.prepareUpdate().exec();
    }

    /**
     * 执行插入
     */
    fun execInsert(): Int {
        var batchInsert = MongoBaseInsertClip(moerEntity.tableName)
        batchInsert.addEntity(entity);

        return batchInsert.exec();
    }

    /**
     * 先更新，如果不存在，则插入。
     * @return: 返回插入的Id，如果是更新则返回空字串
     */
    fun updateOrAdd(): Int {
        //有一个问题，可能是阻止更新了。所以导致是0。
        if (this.execUpdate() == 0) {
            return this.execInsert()
        }
        return db.affectRowCount;
    }

    /**
     * 更新，默认按 id 更新
     */
    fun exec(): Int {
        return updateOrAdd();
    }


    /**
     * 执行更新并返回更新后的数据（适用于更新一条的情况）
     */
    fun saveAndReturnNew(mapFunc: ((Document) -> Unit)? = null): E? {
        return this.prepareUpdate().saveAndReturnNew(this.moerEntity.entityClass, mapFunc)
    }
}

