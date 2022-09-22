package nbcp.db.mongo

import nbcp.comm.*
import nbcp.db.db
import org.bson.Document
import org.springframework.data.mongodb.core.query.Criteria
//import nbcp.comm.*


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
        var sortName = db.mongo.getMongoColumnName(field)

        this.sort.put(sortName, if (asc) 1 else -1)
        return this;
    }

    fun where(whereData: Criteria): MongoQueryClip<M, E> {
        this.whereData.putAll(whereData.criteriaObject);
        return this;
    }

//    fun where(whereData: List<Criteria>): MongoQueryClip<M, E> {
//        this.whereData.addAll(whereData);
//        return this;
//    }

    fun where(where: (M) -> Criteria): MongoQueryClip<M, E> {
        this.whereData.putAll(where(moerEntity).criteriaObject);
        return this;
    }

    /**
     * 从数组对象中查询，并返回数组中的第一个匹配项。
     * @param where: match_elemMatch 操作符返回的对象。
     */
    fun where_select_elemMatch_first_item(where: (M) -> Criteria): MongoQueryClip<M, E> {
        this.where(where);
        return this.select_elemMatch_first_item(where)
    }

    /**
     * 返回数组中的第一个匹配项。
     * @param where: match_elemMatch 操作符返回的对象。
     */
    fun select_elemMatch_first_item(where: (M) -> Criteria): MongoQueryClip<M, E> {
        var doc = where(moerEntity).toDocument();
        this.selectProjections.putAll(doc)
        return this;
    }

    /**
     * 获取Array的某几个
     */
    fun select_array_from_first(select: (M) -> MongoColumnName, skip: Int, take: Int): MongoQueryClip<M, E> {
        return select_array_slice(select(this.moerEntity).toString(), skip, take)
    }

    /**
     * 从Array最后位置获取。
     */
    fun select_array_from_last(select: (M) -> MongoColumnName, take: Int): MongoQueryClip<M, E> {
        return select_array_slice(select(this.moerEntity).toString(), if (take < 0) take else (0 - take))
    }

    /**
     * https://docs.mongodb.com/manual/reference/operator/projection/slice/#proj._S_slice
     */
    private fun select_array_slice(select: String, vararg values: Int): MongoQueryClip<M, E> {
        if (values.isEmpty() || values.size > 2) {
            throw DataInvalidateException()
        }

        var doc = Document();
        var slice = Document();

        if (values.size == 1) {
            slice.put("\$slice", values.first())
        } else {
            slice.put("\$slice", values)
        }

        doc.put(select, slice)
        this.selectProjections.putAll(doc)
        return this;
    }

    fun whereOr(vararg wheres: (M) -> Criteria): MongoQueryClip<M, E> {
        whereOr(*wheres.map { it(moerEntity) }.toTypedArray())
        return this;
    }

    /**
     * 对同一个字段多个条件时使用。
     */
    fun whereAnd(vararg wheres: (M) -> Criteria): MongoQueryClip<M, E> {
        whereAnd(*wheres.map { it(moerEntity) }.toTypedArray())
        return this;
    }

    fun whereIf(whereIf: Boolean, whereData: ((M) -> Criteria)): MongoQueryClip<M, E> {
        if (whereIf == false) return this;

        this.whereData.putAll(whereData(moerEntity).criteriaObject);
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

    fun toTreeJson(
        idColumn: ((M) -> MongoColumnName),
        pidColumn: ((M) -> MongoColumnName),
        pidValue: Serializable,
        childrenFieldName: String = "children"
    ): List<Document> {
        return MyOqlMongoTreeData(
            this,
            db.mongo.getEntityColumnName(idColumn(this.moerEntity).toString()),
            pidColumn(this.moerEntity),
            pidValue,
            childrenFieldName
        ).toTreeJson();
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

