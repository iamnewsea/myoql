package nbcp.db.es

import nbcp.comm.*
import nbcp.db.db

fun <M : EsBaseEntity<T>, T : IEsDocument> M.query(): EsQueryClip<M, T> = EsQueryClip(this);


fun <M : EsBaseEntity<T>, T : IEsDocument> M.queryById(id: String): EsQueryClip<M, T> = this.query()
        .where("id" match id);


//fun <M : EsBaseEntity<E>, E : IEsDocument> M.updateById(id: String): EsUpdateClip<M> {
//    var id = id.trim()
//    if (id.isEmpty()) {
//        throw RuntimeException("按id更新es数据时，id不能为空！")
//    }
//    return EsUpdateClip(this).where("id" to id);
//}

/**
 * 按实体单条更新。 默认使用Id更新。
 */
//fun <M : EsBaseEntity<E>, E : IEsDocument> M.updateWithEntity(entity:E): EsSetEntityUpdateClip<M> {
//    return EsSetEntityUpdateClip(this,entity);
//}

fun <M : EsBaseEntity<E>, E : IEsDocument> M.batchInsert(): EsInsertClip<M,E> {
    return EsInsertClip(this);
}

fun <M : EsBaseEntity<E>, E : IEsDocument> M.update(): EsUpdateClip<M,E> {
    return EsUpdateClip(this);
}

fun <M : EsBaseEntity<E>, E : IEsDocument> M.delete(): EsDeleteClip<M> = EsDeleteClip(this)

//fun <M : EsBaseEntity<E>, E : IEsDocument> M.deleteById(id: String): EsDeleteClip<M> {
//    var ret = EsDeleteClip(this);
//    ret.where("id" to id)
//    return ret;
//}


fun <M : EsBaseEntity<E>, E : IEsDocument> M.aggregate(): EsAggregateClip<M, E> {
    var ret = EsAggregateClip(this);
    return ret;
}


