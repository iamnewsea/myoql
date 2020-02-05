package nbcp.db.mongo

import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.query.Criteria
import nbcp.base.extend.*
import nbcp.db.db
import nbcp.db.mongo.*


fun <M : MongoBaseEntity<T>, T : IMongoDocument> M.query(): MongoQueryClip<M, T> = MongoQueryClip(this);

fun <M : MongoBaseEntity<T>, T : IMongoDocument> M.query(whereData: Criteria): MongoQueryClip<M, T> {
    var ret = MongoQueryClip(this);
    ret.where(whereData);
    return ret;
}

fun <M : MongoBaseEntity<T>, T : IMongoDocument> M.queryById(id: String): MongoQueryClip<M, T> = this.query().where("id" match id);


fun <M : MongoBaseEntity<E>, E : IMongoDocument> M.updateById(id: String): MongoUpdateClip<M> {
    var id = id.trim()
    if (id.isEmpty()) {
        throw RuntimeException("按id更新mongo数据时，id不能为空！")
    }
    return MongoUpdateClip(this).where("id" match id);
}

/**
 * 按实体单条更新。 默认使用Id更新。
 */
fun <M : MongoBaseEntity<E>, E : IMongoDocument> M.updateWithEntity(entity:E): MongoSetEntityUpdateClip<M> {
    return MongoSetEntityUpdateClip(this,entity);
}

fun <M : MongoBaseEntity<E>, E : IMongoDocument> M.insert(): MongoInsertClip<M,E> {
    return MongoInsertClip(this);
}


//
//data class MongoSaveClip<M : MongoBaseEntity<E>, E : IMongoDocument>(var moerEntity: M, var entity: E) {
//    private var whereColumns = MongoColumns()
//    private var unsetColumns = MongoColumns()
//    private var setColumns: MongoColumns? = null
//
//    fun where(where: (M) -> MongoColumnName): MongoSaveClip<M, E> {
//        this.whereColumns.add(where(moerEntity));
//        return this;
//    }
//
//    //仅更新有效
//    fun unset(column: (M) -> MongoColumnName): MongoSaveClip<M, E> {
//        this.unsetColumns.add(column(moerEntity));
//        return this;
//    }
//
//    fun set(column: (M) -> MongoColumnName): MongoSaveClip<M, E> {
//        if( this.setColumns == null){
//            this.setColumns = MongoColumns();
//        }
//        this.setColumns!!.add(column(moerEntity));
//        return this;
//    }
//
//    fun exec(): Int {
//        if (whereColumns.any() == false) {
//            whereColumns.add(MongoColumnName("id"))
//        }
//
//        if (whereColumns.map { it.toString() }.contains("id") && !entity.id.HasValue) {
//            this.moerEntity.insert(entity);
//            return db.affectRowCount;
//        }
//
//        var update = this.moerEntity.update()
//        update.setEntity(entity, { this.whereColumns }, { this.unsetColumns });
//        if (update.exec() == 0) {
//            entity.id = "";
//            this.moerEntity.insert(entity);
//        }
//        return db.affectRowCount;
//    }
//}

//如果存在，就Update，否则 Insert
//fun <M : MongoBaseEntity<E>, E : IMongoDocument> M.save(entity: E): MongoSaveClip<M, E> {
//    return MongoSaveClip<M, E>(this, entity);
//}

fun <M : MongoBaseEntity<E>, E : IMongoDocument> M.updateById(id: ObjectId): MongoUpdateClip<M> {
    var ret = this.update();
    ret.where("id" match id);
    return ret;
}

fun <M : MongoBaseEntity<E>, E : IMongoDocument> M.update(): MongoUpdateClip<M> {
    return MongoUpdateClip(this);
}

fun <M : MongoBaseEntity<E>, E : IMongoDocument> M.delete(): MongoDeleteClip<M> = MongoDeleteClip(this)

fun <M : MongoBaseEntity<E>, E : IMongoDocument> M.deleteById(id: String): MongoDeleteClip<M> {
    var ret = MongoDeleteClip(this);
    ret.where("id" match id)
    return ret;
}


fun <M : MongoBaseEntity<E>, E : IMongoDocument> M.aggregate(): MongoAggregateClip<M, E> {
    var ret = MongoAggregateClip(this);
    return ret;
}


