@file:JvmName("MyOqlMongo")
@file:JvmMultifileClass

package nbcp.myoql.db.mongo

import nbcp.myoql.db.mongo.component.MongoBaseMetaCollection
import nbcp.myoql.db.mongo.extend.match

import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.query.Criteria


fun <M : MongoBaseMetaCollection<T>, T:Any > M.query(): MongoQueryClip<M, T> = MongoQueryClip(this);

fun <M : MongoBaseMetaCollection<T>, T:Any > M.query(whereData: Criteria): MongoQueryClip<M, T> {
    var ret = MongoQueryClip(this);
    ret.where(whereData);
    return ret;
}

fun <M : MongoBaseMetaCollection<T>, T :Any> M.queryById(id: String): MongoQueryClip<M, T> =
    this.query().where("id" match id);


fun <M : MongoBaseMetaCollection<out E>,E:Any> M.updateById(id: String): MongoUpdateClip<M,E> {
    var idValue = id.trim()
    if (idValue.isEmpty()) {
        throw RuntimeException("按id更新mongo数据时，id不能为空！")
    }
    return MongoUpdateClip(this).where("id" match idValue);
}

/**
 * 按实体单条更新。 默认使用Id更新，不会更新id。
 */
fun <M : MongoBaseMetaCollection<out E>,E:Any> M.updateWithEntity(entity: E): MongoSetEntityUpdateClip<M,E> {
    return MongoSetEntityUpdateClip(this, entity);
}


fun <M : MongoBaseMetaCollection<out Any>> M.batchInsert(): MongoInsertClip<M> {
    return MongoInsertClip(this);
}


fun <M : MongoBaseMetaCollection<out E>,E:Any> M.updateById(id: ObjectId): MongoUpdateClip<M,E> {
    var ret = this.update();
    ret.where("id" match id);
    return ret;
}

fun <M : MongoBaseMetaCollection<out E>,E:Any> M.update(): MongoUpdateClip<M,E> {
    return MongoUpdateClip(this);
}

fun <M : MongoBaseMetaCollection<out Any>> M.delete(): MongoDeleteClip<M> = MongoDeleteClip(this)

fun <M : MongoBaseMetaCollection<out Any>> M.deleteById(id: String): MongoDeleteClip<M> {
    var ret = MongoDeleteClip(this);
    ret.where("id" match id)
    return ret;
}


fun <M : MongoBaseMetaCollection<E>, E:Any> M.aggregate(): MongoAggregateClip<M, E> {
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