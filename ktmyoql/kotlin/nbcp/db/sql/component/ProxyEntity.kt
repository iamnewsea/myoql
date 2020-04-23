package nbcp.db.sql

import nbcp.comm.*
import nbcp.utils.*
import nbcp.db.db
import nbcp.db.sql.*
import java.io.Serializable
import kotlin.reflect.full.memberProperties

//import nbcp.db.mongo.entity.IMongoDocument


fun <M : SqlBaseTable<out T>, T : IBaseDbEntity> M.query(selectColumn: ((M) -> SqlColumnName)? = null): SqlQueryClip<M, T> {
    var ret = SqlQueryClip<M, T>(this);
    if (selectColumn != null) {
        ret.select { selectColumn(this) }
    }
    return ret;
}

fun <M : SqlBaseTable<out T>, T : IBaseDbEntity> M.delete(): SqlDeleteClip<M, T> {
    return SqlDeleteClip<M, T>(this);
}

fun <M : SqlBaseTable<out T>, T : IBaseDbEntity> M.update(): SqlUpdateClip<M, T> {
    return SqlUpdateClip<M, T>(this);
}

//自增主键 ,返回到 entity 实体上. 以及 dbr.lastAutoId
//返回 影响行数
fun <M : SqlBaseTable<out T>, T : IBaseDbEntity> M.doInsert(entity: T): Int {
    return SqlInsertClip(this).add(entity).exec()
}

fun <M : SqlBaseTable<out T>, T : IBaseDbEntity> M.batchInsert(): SqlInsertClip<M, T> {
    return SqlInsertClip(this)
}


fun <M : SqlBaseTable<out T>, T : IBaseDbEntity> M.insertIfNotExists(entity: T, unionKey: ((M) -> SqlColumnNames)): Int {
    var map = entity.ConvertJson(JsonMap::class.java)

    var query = this.query();
    var uks = unionKey(this);

    uks.forEach { key ->
        var value = map.get(key.name)
        if (value == null) {
            query.where { key.isNullOrEmpty() }
        } else {
            query.where { key match value as Serializable }
        }
    }

    if (query.exists()) {
        return 0;
    }
    return SqlInsertClip(this).add(entity).exec()
}


/**
 * 自动保存.: 先更新, 再插入
 */
fun <M : SqlBaseTable<out T>, T : IBaseDbEntity> M.save(entity: T, unionKey: ((M) -> SqlColumnNames)): Int {
    var map = entity.ConvertJson(JsonMap::class.java)

    var update = this.update();
    var uks = unionKey(this);

    uks.forEach { key ->
        var value = map.get(key.name)
        if (value == null) {
            update.where { key.isNullOrEmpty() }
        } else {
            update.where { key match value as Serializable }
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
    return SqlInsertClip(this).add(entity).exec()
}

