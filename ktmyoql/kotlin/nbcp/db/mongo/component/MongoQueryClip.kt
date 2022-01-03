package nbcp.db.mongo

import nbcp.comm.*
import nbcp.db.db
import nbcp.db.sql.MyOqlSqlTreeData
import nbcp.db.sql.SqlColumnName
import org.bson.Document
import org.springframework.data.mongodb.core.query.Criteria
//import nbcp.comm.*


import org.slf4j.LoggerFactory
import java.io.Serializable

/**
 * MongoQuery
 */
class MongoQueryClip<M : MongoBaseMetaCollection<E>, E : Any>(var moerEntity: M) :
    MongoBaseQueryClip(moerEntity.tableName) {


    fun limit(skip: Int, take: Int): MongoQueryClip<M, E> {
        this.skip = skip;
        this.take = take;
        return this;
    }

    /**
     * 升序
     */
    fun orderByAsc(sortFunc: (M) -> MongoColumnName): MongoQueryClip<M, E> {
        return this.orderBy(true, sortFunc(this.moerEntity).toString())
    }

    /**
     * 降序
     */
    fun orderByDesc(sortFunc: (M) -> MongoColumnName): MongoQueryClip<M, E> {
        return this.orderBy(false, sortFunc(this.moerEntity).toString())
    }

    fun orderBy(asc: Boolean, field: String): MongoQueryClip<M, E> {
        var sortName = field
        if (sortName == "id") {
            sortName = "_id"
        } else if (sortName.endsWith(".id")) {
            sortName = sortName.slice(0..sortName.length - 3) + "._id";
        }

        this.sort.put(sortName, if (asc) 1 else -1)
        return this;
    }

    fun where(whereData: Criteria): MongoQueryClip<M, E> {
        this.whereData.add(whereData.criteriaObject);
        return this;
    }

//    fun where(whereData: List<Criteria>): MongoQueryClip<M, E> {
//        this.whereData.addAll(whereData);
//        return this;
//    }

    fun where(where: (M) -> Criteria): MongoQueryClip<M, E> {
        this.whereData.add(where(moerEntity).criteriaObject);
        return this;
    }

    fun selectWhere_elemMatch(where: (M) -> Criteria): MongoQueryClip<M, E> {
        this.where(where);
        return this.select_elemMatch(where)
    }

    fun select_elemMatch(where: (M) -> Criteria): MongoQueryClip<M, E> {
        var doc = where(moerEntity).toDocument();
        this.selectProjections.putAll(doc)
        return this;
    }

    /**
     * 获取Array的某几个
     */
    fun select_array_from_first(select: (M) -> MongoColumnName, skip: Int, take: Int): MongoQueryClip<M, E> {
        return select_array_slice(select(this.moerEntity).toString(), false, skip, take)
    }

    /**
     * 从Array最后位置获取。
     */
    fun select_array_from_last(select: (M) -> MongoColumnName, skip: Int, take: Int): MongoQueryClip<M, E> {
        return select_array_slice(select(this.moerEntity).toString(), true, skip, take)
    }


    private fun select_array_slice(select: String, isFromLast: Boolean, skip: Int, take: Int): MongoQueryClip<M, E> {
        var doc = Document();
        var offset = 0;
        if (isFromLast) {
            offset = -1;
        }

        var slice = Document();
        slice.put("\$slice", listOf(skip - offset, take))

        doc.put(select, slice)
        this.selectProjections.putAll(doc)
        return this;
    }

    fun whereOr(vararg wheres: (M) -> Criteria): MongoQueryClip<M, E> {
        return whereOr(*wheres.map { it(moerEntity) }.toTypedArray())
    }

    fun whereOr(vararg wheres: Criteria): MongoQueryClip<M, E> {
        if (wheres.any() == false) return this;
        val where = Criteria();
        where.orOperator(*wheres)
        this.whereData.add(where.criteriaObject);
        return this;
    }

    fun whereIf(whereIf: Boolean, whereData: ((M) -> Criteria)): MongoQueryClip<M, E> {
        if (whereIf == false) return this;

        this.whereData.add(whereData(moerEntity).criteriaObject);
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


    @JvmOverloads
    fun toList(mapFunc: ((Document) -> Unit)? = null): MutableList<E> {
        return toList(moerEntity.entityClass, mapFunc)
    }

    @JvmOverloads
    fun toEntity(mapFunc: ((Document) -> Unit)? = null): E? {
        this.take = 1;
        return toList(moerEntity.entityClass, mapFunc).firstOrNull();
    }

//    inline fun <reified R> toEntity(): R? {
//        return toEntity(R::class.java);
//    }

    fun queryTree(
        pidValue: Serializable,
        idColumn: ((M) -> MongoColumnName),
        pidColumn: ((M) -> MongoColumnName)
    ): MyOqlMongoTreeData<M, E> {
        return MyOqlMongoTreeData(
            this,
            pidValue,
            db.mongo.getEntityColumnName(idColumn(this.moerEntity).toString()),
            pidColumn(this.moerEntity)
        );
    }

    @JvmOverloads
    fun <R> toEntity(clazz: Class<R>, mapFunc: ((Document) -> Unit)? = null): R? {
        this.take = 1;
        return toList(clazz, mapFunc).firstOrNull();
    }

    @JvmOverloads
    fun toListResult(mapFunc: ((Document) -> Unit)? = null): ListResult<E> {
        return toListResult(this.moerEntity.entityClass, mapFunc);
    }

    // ------------------------------
    fun unSelect(column: MongoColumnName): MongoQueryClip<M, E> {
        unSelectColumns.add(column.toString());
        return this;
    }
}

