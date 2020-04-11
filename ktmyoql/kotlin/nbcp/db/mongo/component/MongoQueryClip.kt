package nbcp.db.mongo

import com.mongodb.DBCursor
import com.mongodb.client.model.DBCollectionFindOptions
import nbcp.comm.*
import org.bson.Document
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import nbcp.comm.*
//import nbcp.comm.*
import nbcp.base.extend.*


import nbcp.db.mongo.*
import org.slf4j.LoggerFactory

/**
 * MongoQuery
 */
class MongoQueryClip<M : MongoBaseEntity<E>, E : IMongoDocument>(var moerEntity: M) : MongoBaseQueryClip(moerEntity.tableName) {


    fun limit(skip: Int, take: Int): MongoQueryClip<M, E> {
        this.skip = skip;
        this.take = take;
        return this;
    }

    /**
     * 升序
     */
    fun orderByAsc(sortFunc: (M) -> MongoColumnName): MongoQueryClip<M, E> {
        return this.orderBy(true, sortFunc(this.moerEntity))
    }

    /**
     * 降序
     */
    fun orderByDesc(sortFunc: (M) -> MongoColumnName): MongoQueryClip<M, E> {
        return this.orderBy(false, sortFunc(this.moerEntity))
    }

    private fun orderBy(asc: Boolean, field: MongoColumnName): MongoQueryClip<M, E> {
        var sortName = field.toString()
        if (sortName == "id") {
            sortName = "_id"
        } else if (sortName.endsWith(".id")) {
            sortName = sortName.slice(0..sortName.length - 3) + "._id";
        }

        this.sort.put(sortName, if (asc) 1 else -1)
        return this;
    }

    fun where(whereData: Criteria): MongoQueryClip<M, E> {
        this.whereData.add(whereData);
        return this;
    }

    fun where(whereData: List<Criteria>): MongoQueryClip<M, E> {
        this.whereData.addAll(whereData);
        return this;
    }

    fun where(where: (M) -> Criteria): MongoQueryClip<M, E> {
        this.whereData.add(where(moerEntity));
        return this;
    }

    fun whereOr(vararg wheres: (M) -> Criteria): MongoQueryClip<M, E> {
        return whereOr(*wheres.map { it(moerEntity) }.toTypedArray())
    }

    fun whereOr(vararg wheres: Criteria): MongoQueryClip<M, E> {
        if (wheres.any() == false) return this;
        var where = Criteria();
        where.orOperator(*wheres)
        this.whereData.add(where);
        return this;
    }

    fun whereIf(whereIf: Boolean, whereData: ((M) -> Criteria)): MongoQueryClip<M, E> {
        if (whereIf == false) return this;

        this.whereData.add(whereData(moerEntity));
        return this;
    }

    fun select(vararg columns: MongoColumnName): MongoQueryClip<M, E> {
        selectColumns.addAll(columns.map { it.toString() })
        return this;
    }


    fun select(vararg columns: String): MongoQueryClip<M, E> {
        selectColumns.addAll(columns)
        return this;
    }

    fun select(column: (M) -> MongoColumnName): MongoQueryClip<M, E> {
        this.selectColumns.add(column(moerEntity).toString());
        return this;
    }

    fun unSelect(column: (M) -> MongoColumnName): MongoQueryClip<M, E> {
        unSelectColumns.add(column(this.moerEntity).toString());
        return this;
    }

    companion object {
        private val logger by lazy {
            return@lazy LoggerFactory.getLogger(this::class.java)
        }
    }


    fun toList(mapFunc: ((Document) -> Unit)? = null): MutableList<E> {
        return toList(moerEntity.entityClass, mapFunc)
    }

    fun toEntity(mapFunc: ((Document) -> Unit)? = null): E? {
        this.take = 1;
        return toList(moerEntity.entityClass, mapFunc).firstOrNull();
    }

//    inline fun <reified R> toEntity(): R? {
//        return toEntity(R::class.java);
//    }

    fun <R> toEntity(clazz: Class<R>, mapFunc: ((Document) -> Unit)? = null): R? {
        this.take = 1;
        return toList(clazz, mapFunc).firstOrNull();
    }


    fun toListResult(mapFunc: ((Document) -> Unit)? = null): ListResult<E> {
        return toListResult(this.moerEntity.entityClass, mapFunc);
    }

    // ------------------------------
    fun unSelect(column: MongoColumnName): MongoQueryClip<M, E> {
        unSelectColumns.add(column.toString());
        return this;
    }
}

