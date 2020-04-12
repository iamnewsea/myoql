package nbcp.db.mongo


import org.bson.types.ObjectId
import nbcp.comm.*
import nbcp.utils.*
import nbcp.db.db
import nbcp.db.mongo.*
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.query.Criteria

/**
 * Created by udi on 17-4-7.
 */

//根据Id，更新Mongo的一个键。

/**
 * MongoUpdate
 */
class MongoSetEntityUpdateClip<M : MongoBaseEntity<out IMongoDocument>>(var moerEntity: M, var entity: IMongoDocument) : MongoBaseUpdateClip(moerEntity.tableName) {
    companion object {
        private val logger by lazy {
            return@lazy LoggerFactory.getLogger(this::class.java)
        }
    }

    private var whereColumns = mutableSetOf<String>()
    private var setColumns = mutableSetOf<String>()
    private var unsetColumns = mutableSetOf<String>()

    fun withColumn(setFunc: (M) -> MongoColumnName): MongoSetEntityUpdateClip<M> {
        this.setColumns.add(setFunc(this.moerEntity).toString())
        return this;
    }

    fun withoutColumn(unsetFunc: (M) -> MongoColumnName): MongoSetEntityUpdateClip<M> {
        this.unsetColumns.add(unsetFunc(this.moerEntity).toString())
        return this;
    }

    fun whereColumn(whereFunc: (M) -> MongoColumnName): MongoSetEntityUpdateClip<M> {
        this.whereColumns.add(whereFunc(this.moerEntity).toString())
        return this;
    }

    fun withRequestParams(keys: Set<String>): MongoSetEntityUpdateClip<M> {
        keys.forEach { key ->
            withColumn { MongoColumnName(key) }
        }
        return this
    }

    //额外设置
    fun set(setItemAction: (M) -> Pair<MongoColumnName, Any?>): MongoSetEntityUpdateClip<M> {
        var setItem = setItemAction(this.moerEntity);
        this.setData.set(setItem.first.toString(), setItem.second);
        return this;
    }

    fun where(where: (M) -> Criteria): MongoSetEntityUpdateClip<M> {
        this.whereData.add(where(moerEntity));
        return this;
    }


    override fun exec(): Int {
        if (whereColumns.any() == false) {
            whereColumns.add("_id")
        }

        var whereData2 = LinkedHashMap<String, Any?>()
        var setData2 = LinkedHashMap<String, Any?>()

        var update = MongoUpdateClip(this.moerEntity)
        this.entity::class.java.AllFields.forEach {
            var findKey = it.name;
            if (it.name == "id") {
                findKey = "_id"
            }

            if (whereColumns.contains(findKey)) {
                var value = MyUtil.getPrivatePropertyValue(this.entity, it.name);
                whereData2.put(it.name, value)
                return@forEach
            }

            if (it.name == "id") {
                return@forEach
            }

            if (setColumns.any() && (setColumns.contains(it.name) == false)) {
                return@forEach;
            }

            if (unsetColumns.contains(it.name)) {
                return@forEach
            }

            var value = MyUtil.getPrivatePropertyValue(this.entity, it.name);
            setData2.put(it.name, value)
        }

        var setKeys = this.whereData.map { it.toDocument().keys.toTypedArray() }.Unwind();
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
        return update.exec();
    }

    /**
     * 先更新，如果不存在，则插入。
     * @return: 返回插入的Id，如果是更新则返回空字串
     */
    fun updateOrAdd(): String {
        var ret = "";

        //有一个问题，可能是阻止更新了。所以导致是0。
        if (this.exec() == 0) {
            var batchInsert = MongoBaseInsertClip(moerEntity.tableName)
            batchInsert.addEntity(entity);

            db.affectRowCount = batchInsert.exec();
            ret = entity.id;
        }
        return ret;
    }
}

