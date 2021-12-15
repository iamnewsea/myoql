package nbcp.db.mongo


import nbcp.comm.*
import nbcp.utils.*
import nbcp.db.db
import org.springframework.data.mongodb.core.query.Criteria

/**
 * Created by udi on 17-4-7.
 */

/**
 * 只是简单的更新实体。更多操作要转化为 Update 再操作。 castToUpdate
 * 不会更新 id
 */
class MongoSetEntityUpdateClip<M : MongoBaseMetaCollection<out E>, E : Any>(
        var moerEntity: M,
        var entity: E
) : MongoClipBase(moerEntity.tableName) {

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

    /**
     * 转为 Update子句，执行更多 Update 命令。
     */
    fun castToUpdate(): MongoUpdateClip<M,E> {
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
                    if( value == null){
                        return@forEach
                    }
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

                if( value == null){
                    return@forEach
                }
                setData2.put(findKey, value)
            }
        }

        whereData2.forEach { key, value ->
            update.where(key match value);
        }

        setData2.forEach { key, value ->
            update.set(key, value);
        }

        return update;
    }

    /**
     * 执行更新, == exec ,语义清晰
     */
    fun execUpdate(): Int {
        return this.castToUpdate().exec();
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
}

