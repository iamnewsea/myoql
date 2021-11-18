package nbcp.db.es

import java.io.Serializable

fun <M : EsBaseMetaEntity<T>, T : Serializable> M.query(): EsQueryClip<M, T> = EsQueryClip(this);


fun <M : EsBaseMetaEntity<T>, T : Serializable> M.queryById(id: String): EsQueryClip<M, T> = this.query()
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

fun <M : EsBaseMetaEntity<E>, E : Serializable> M.batchInsert(): EsInsertClip<M, E> {
    return EsInsertClip(this);
}

fun <M : EsBaseMetaEntity<E>, E : Serializable> M.update(): EsUpdateClip<M, E> {
    return EsUpdateClip(this);
}

fun <M : EsBaseMetaEntity<E>, E : Serializable> M.delete(): EsDeleteClip<M> = EsDeleteClip(this)

//fun <M : EsBaseEntity<E>, E : Serializable> M.deleteById(id: String): EsDeleteClip<M> {
//    var ret = EsDeleteClip(this);
//    ret.where("id" to id)
//    return ret;
//}


fun <M : EsBaseMetaEntity<E>, E : Serializable> M.aggregate(): EsAggregateClip<M, E> {
    val ret = EsAggregateClip(this);
    return ret;
}


