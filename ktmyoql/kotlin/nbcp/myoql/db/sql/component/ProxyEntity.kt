package nbcp.myoql.db.sql.component

import nbcp.base.comm.JsonMap
import nbcp.base.extend.*
import nbcp.base.extend.Minus
import nbcp.myoql.db.db
import nbcp.myoql.db.sql.base.BaseAliasSqlSect
import nbcp.myoql.db.sql.base.SqlBaseMetaTable
import nbcp.myoql.db.sql.base.SqlColumnNames
import java.io.Serializable

//import nbcp.myoql.db.mongo.entity.Serializable

@JvmOverloads
fun <M : SqlBaseMetaTable<T>, T : Serializable> M.query(selectColumn: ((M) -> BaseAliasSqlSect)? = null): SqlQueryClip<M, T> {
    var ret = SqlQueryClip(this);
    if (selectColumn != null) {
        ret.select { selectColumn(this) }
    }
    return ret;
}

fun <M : SqlBaseMetaTable<out Serializable>> M.delete(): SqlDeleteClip<M> {
    return SqlDeleteClip<M>(this);
}

fun <M : SqlBaseMetaTable<out Serializable>> M.update(): SqlUpdateClip<M> {
    return SqlUpdateClip<M>(this);
}

fun <M : SqlBaseMetaTable<out T>, T : Serializable> M.updateWithEntity(entity: T): SqlSetEntityUpdateClip<M> {
    return SqlSetEntityUpdateClip<M>(this, entity);
}

//自增主键 ,返回到 entity 实体上. 以及 dbr.lastAutoId
//返回 影响行数
fun <M : SqlBaseMetaTable<out T>, T : Serializable> M.doInsert(entity: T): Int {
    return SqlInsertClip(this).addEntity(entity).exec()
}

fun <M : SqlBaseMetaTable<out T>, T : Serializable> M.batchInsert(): SqlInsertClip<M, T> {
    return SqlInsertClip(this)
}


fun <M : SqlBaseMetaTable<T>, T : Serializable> M.insertIfNotExists(
    entity: T,
    unionKey: ((M) -> SqlColumnNames)
): Int {
    var map = entity.ConvertJson(JsonMap::class.java)

    var query = this.query()
    var uks = unionKey(this);

    uks.forEach { key ->
        var value = map.get(key.name)
        if (value == null) {
            query.where { key.isNullOrEmpty() }
        } else {
            query.where { key sqlEquals value as Serializable }
        }
    }

    if (query.exists()) {
        return 0;
    }
    return SqlInsertClip(this).addEntity(entity).exec()
}


/**
 * 自动保存.: 先更新, 再插入
 */
fun <M : SqlBaseMetaTable<out T>, T : Serializable> M.save(entity: T, unionKey: ((M) -> SqlColumnNames)): Int {
    var map = entity.ConvertJson(JsonMap::class.java)

    val update = this.update();
    val uks = unionKey(this);

    uks.forEach { key ->
        val value = map.get(key.name)
        if (value == null) {
            update.where { key.isNullOrEmpty() }
        } else {
            update.where { key sqlEquals value as Serializable }
        }
    }

    this.getColumns()
        .filter { it.name != this.getAutoIncrementKey() }
        .Minus(uks) { a, b -> a.equals(b) }
        .forEach { key ->
            update.set { key to map.getValue(key.name) as Serializable }
        }

    update.exec()
    if (db.affectRowCount > 0) {
        return db.affectRowCount;
    }
    return SqlInsertClip(this).addEntity(entity).exec()
}