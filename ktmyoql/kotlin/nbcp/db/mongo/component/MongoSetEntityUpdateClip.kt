package nbcp.db.mongo


import org.bson.types.ObjectId
import nbcp.base.extend.HasValue
import nbcp.base.extend.*
import nbcp.base.utils.MyUtil
import nbcp.db.db
import nbcp.db.mongo.*
import org.slf4j.LoggerFactory

/**
 * Created by udi on 17-4-7.
 */

//根据Id，更新Mongo的一个键。

/**
 * MongoUpdate
 */
class MongoSetEntityUpdateClip<M : MongoBaseEntity<out IMongoDocument>>(var moerEntity: M, var entity: IMongoDocument) : MongoClipBase(moerEntity.tableName), IMongoWhereable {
    companion object {
        private val logger by lazy {
            return@lazy LoggerFactory.getLogger(this::class.java)
        }
    }

    private var whereColumns = mutableSetOf<String>()
    private var setColumns = mutableSetOf<String>()
    private var unsetColumns = mutableSetOf<String>()
    /**
     * @param entity 要更新的实体
     * @param whereColumnsFunc where 列。
     * @param unsetColumnsFunc 排除要更新的列。
     */
//    fun setEntity(entity: IMongoDocument): MongoSetEntityUpdateClip<M> {
//        this.entity = entity;
//        return this;
//    }

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

    fun exec(): Int {
//        if (this.entity == null) {
//            throw RuntimeException("entity不能为空")
//        }

        if (whereColumns == null) {
            whereColumns = mutableSetOf("_id");
        }

        var update = MongoUpdateClip(this.moerEntity)
        this.entity::class.java.AllFields.forEach {
            var findKey = it.name;
            if (it.name == "id") {
                findKey = "_id"
            }

            if (whereColumns!!.contains(findKey)) {
                var value = MyUtil.getPrivatePropertyValue(this.entity, it.name);
                if (value != null) {
                    if (value is String) {
                        if (value.HasValue) {
                            update.where(it.name match value);
                        }
                    } else {
                        update.where(it.name match value);
                    }
                }

                return@forEach
            }

            if (it.name == "id") {
                return@forEach
            }

            if (setColumns != null && !setColumns!!.contains(it.name)) {
                return@forEach;
            }

            if (unsetColumns.contains(it.name)) {
                return@forEach
            }

            var value = MyUtil.getPrivatePropertyValue(this.entity, it.name);
//            if (value == null) {
//                return@forEach
//            }
            update.set(it.name, value);
        }

        return update.exec();
    }

    /**
     * 先更新，如果不存在，则插入。
     * @return: 返回插入的Id，如果是更新则返回空字串
     */
    fun updateOrAdd(): String {
        var ret = "";
        if (this.exec() == 0) {
            if (entity.id.isEmpty()) {
                entity.id = ObjectId().toString();
            }

            mongoTemplate.insert(entity, this.moerEntity.tableName);
            db.affectRowCount = 1;
            ret = entity.id;
        }
        return ret;
    }
}

