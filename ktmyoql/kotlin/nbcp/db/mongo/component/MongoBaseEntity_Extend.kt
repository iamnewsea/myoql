@file:JvmName("MyOqlMongo")
@file:JvmMultifileClass

package nbcp.db.mongo
import java.io.Serializable

import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.query.Criteria


fun <M : MongoBaseMetaCollection<T>, T : Serializable> M.query(): MongoQueryClip<M, T> = MongoQueryClip(this);

fun <M : MongoBaseMetaCollection<T>, T : Serializable> M.query(whereData: Criteria): MongoQueryClip<M, T> {
    var ret = MongoQueryClip(this);
    ret.where(whereData);
    return ret;
}

fun <M : MongoBaseMetaCollection<T>, T : Serializable> M.queryById(id: String): MongoQueryClip<M, T> = this.query().where("id" match id);


fun <M : MongoBaseMetaCollection<E>, E : Serializable> M.updateById(id: String): MongoUpdateClip<M> {
    var id = id.trim()
    if (id.isEmpty()) {
        throw RuntimeException("按id更新mongo数据时，id不能为空！")
    }
    return MongoUpdateClip(this).where("id" match id);
}

/**
 * 按实体单条更新。 默认使用Id更新，不会更新id。
 */
fun <M : MongoBaseMetaCollection<E>, E : Serializable> M.updateWithEntity(entity: E): MongoSetEntityUpdateClip<M> {
    return MongoSetEntityUpdateClip(this, entity);
}

fun <M : MongoBaseMetaCollection<E>, E : Serializable> M.batchInsert(): MongoInsertClip<M, E> {
    return MongoInsertClip(this);
}


fun <M : MongoBaseMetaCollection<E>, E : Serializable> M.updateById(id: ObjectId): MongoUpdateClip<M> {
    var ret = this.update();
    ret.where("id" match id);
    return ret;
}

fun <M : MongoBaseMetaCollection<E>, E : Serializable> M.update(): MongoUpdateClip<M> {
    return MongoUpdateClip(this);
}

fun <M : MongoBaseMetaCollection<E>, E : Serializable> M.delete(): MongoDeleteClip<M> = MongoDeleteClip(this)

fun <M : MongoBaseMetaCollection<E>, E : Serializable> M.deleteById(id: String): MongoDeleteClip<M> {
    var ret = MongoDeleteClip(this);
    ret.where("id" match id)
    return ret;
}


fun <M : MongoBaseMetaCollection<E>, E : Serializable> M.aggregate(): MongoAggregateClip<M, E> {
    var ret = MongoAggregateClip(this);
    return ret;
}


///**
// * 如果存在，就Update，否则 Insert
// */
//fun <M : MongoBaseEntity<E>, E : Serializable> M.save(entity: E, unionKey: ((M) -> MongoColumnName)): Int {
//    var ret = this.updateWithEntity(entity).whereColumn(unionKey).exec()
//    if (ret > 0) return ret;
//    this.doInsert(entity);
//    ret = db.affectRowCount;
//    return ret;
//}