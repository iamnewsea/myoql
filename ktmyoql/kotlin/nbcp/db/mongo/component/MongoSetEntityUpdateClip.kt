package nbcp.db.mongo


import nbcp.comm.*
import nbcp.db.db

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

    private var requestJson: Map<String, Any?> = mapOf()

    /*
    以 duty 列为例 ：
    如果duty 指定为 where 列：
        如果duty没有指定set，忽略，不set
        如果duty指定了 set， set
    else = 如果 duty 没有指定在where 列
        直接 set

        ==》
     如果duty指定了 set，则 set
     如果duty没有指定set，则：    如果duty在where列，则不set，否则set
     */
    private var whereColumns = mutableSetOf<String>()
    private var setColumns = mutableSetOf<String>()
    private var unsetColumns = mutableSetOf<String>("id","createAt")

    fun withColumn(setFunc: (M) -> MongoColumnName): MongoSetEntityUpdateClip<M, E> {
        this.setColumns.add(setFunc(this.moerEntity).toString())
        return this;
    }

    fun withColumns(vararg column: String): MongoSetEntityUpdateClip<M, E> {
        this.setColumns.addAll(column)
        return this;
    }


    fun withoutColumn(setFunc: (M) -> MongoColumnName): MongoSetEntityUpdateClip<M, E> {
        this.unsetColumns.add(setFunc(this.moerEntity).toString())
        return this;
    }

    fun withoutColumns(vararg column: String): MongoSetEntityUpdateClip<M, E> {
        this.unsetColumns.addAll(column)
        return this;
    }

    fun whereColumn(whereFunc: (M) -> MongoColumnName): MongoSetEntityUpdateClip<M, E> {
        this.whereColumns.add(whereFunc(this.moerEntity).toString())
        return this;
    }

    fun withRequestJson(json: Map<String, Any?>): MongoSetEntityUpdateClip<M, E> {
        this.requestJson = json;
        return this;
    }


    fun joinWbsPath(a: String, b: String): String {
        if (a.isEmpty()) return b;
        return a + "." + b;
    }

    /**
     * @param callback ,返回 false 表示停止递归子对象
     */
    fun recursionJson(
        map: Map<String, Any?>,
        pWbs: String,
        callback: (Map<String, Any?>, String, Any, String) -> Unit
    ) {
        map.forEach { kv ->
            val subValue = kv.value;
            if (subValue == null) {
                return@forEach
            }

            callback(map, kv.key, subValue, pWbs)

            if (subValue is Map<*, *>) {
                recursionJson(
                    subValue as Map<String, Any?>,
                    joinWbsPath(pWbs, kv.key),
                    callback
                )
            }
        }
    }

    /**
     * 转为 Update子句，执行更多 Update 命令。
     */
    fun castToUpdate(): MongoUpdateClip<M, E> {
        if (whereColumns.any() == false) {
            whereColumns.add("id")
        }

        var whereData2 = LinkedHashMap<String, Any?>()
        var setData2 = LinkedHashMap<String, Any?>()

        var update = MongoUpdateClip(this.moerEntity)

        //统一按map处理
//        var map = this.entity.ToJson().FromJson<JsonMap>()!!

        //去除空值，仅key有用；大部分情况下，可以按这个对象更新；涉及到object,array，则需要从实体entity中获取！
//        this.withRequestBody = this.withRequestBody.ToJson().FromJson<JsonMap>()!!

        //如果是数组，则直接设置

        var ori_entity_map = this.entity.ToJson().FromJson<JsonMap>()!!

        var withRequestJson = this.requestJson.keys.any();

        recursionJson(if (withRequestJson) this.requestJson else ori_entity_map, "") { map, key, value, pWbs ->

            var wbs = joinWbsPath(pWbs, key);

            var keyInWhere = false;
            if (whereColumns.contains(wbs)) {
                keyInWhere = true;
                if (key == "_id") {
                    whereData2.put(joinWbsPath(pWbs, "id"), value)
                } else {
                    whereData2.put(wbs, value)
                }
            }


            if (unsetColumns.any { it == wbs || wbs.startsWith(it + ".") }) {
                return@recursionJson;
            }

            if (setColumns.contains(wbs) || !keyInWhere) {
                if (key == "_id") {
                    setData2.put(joinWbsPath(pWbs, "id"), value)
                } else {
                    val value_type = value::class.java;

                    if (value_type.IsSimpleType()) {
                        setData2.put(wbs, value)
                    } else if (value_type.isArray || value_type.IsCollectionType) {
                        var ent_wbs_value: Any? = value;
                        if (withRequestJson) {
                            ent_wbs_value = ori_entity_map.getValueByWbsPath(wbs);
                        }

                        if (ent_wbs_value != null) {
                            setData2.put(wbs, ent_wbs_value)
                        }
                    }
                }
            }


            return@recursionJson;
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

