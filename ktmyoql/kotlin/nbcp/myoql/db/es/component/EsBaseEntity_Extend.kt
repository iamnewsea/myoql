package nbcp.myoql.db.es.component

import nbcp.myoql.db.es.EsBulkUpdateClip
import nbcp.myoql.db.es.base.EsColumnName
import java.io.Serializable

fun <M : EsBaseMetaEntity<T>, T : Serializable> M.query(): EsQueryClip<M, T> = EsQueryClip(this);


fun <M : EsBaseMetaEntity<T>, T : Serializable> M.queryById(id: String): EsQueryClip<M, T> =
    this.query()
    .must({ EsColumnName("id") term id })


//fun <M : EsBaseEntity<E>, E : Serializable> M.updateById(id: String): EsUpdateClip<M> {
//    var id = id.trim()
//    if (id.isEmpty()) {
//        throw RuntimeException("按id更新es数据时，id不能为空！")
//    }
//    return EsUpdateClip(this).where("id" to id);
//}

/**
 * 按实体单条更新。 默认使用Id更新。
 */
//fun <M : EsBaseEntity<E>, E : Serializable> M.updateWithEntity(entity:E): EsSetEntityUpdateClip<M> {
//    return EsSetEntityUpdateClip(this,entity);
//}

fun <M : EsBaseMetaEntity<E>, E : Serializable> M.bulkInsert(): EsBulkInsertClip<M, E> {
    return EsBulkInsertClip(this);
}

fun <M : EsBaseMetaEntity<E>, E : Serializable> M.bulkUpdate(): EsBulkUpdateClip<M, E> {
    return EsBulkUpdateClip(this);
}

fun <M : EsBaseMetaEntity<E>, E : Serializable> M.bulkDelete(): EsBulkDeleteClip<M> = EsBulkDeleteClip(this)

//fun <M : EsBaseEntity<E>, E : Serializable> M.deleteById(id: String): EsDeleteClip<M> {
//    var ret = EsDeleteClip(this);
//    ret.where("id" to id)
//    return ret;
//}


fun <M : EsBaseMetaEntity<E>, E : Serializable> M.aggregate(): EsAggregateClip<M, E> {
    val ret = EsAggregateClip(this);
    return ret;
}


