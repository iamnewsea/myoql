package nbcp.db.es

import nbcp.comm.*
import java.io.Serializable

/**
 * EsQuery
 * https://www.elastic.co/guide/en/elasticsearch/reference/7.6/search.html
 */
class EsQueryClip<M : EsBaseMetaEntity<E>, E : Serializable>(var moerEntity: M) :
    EsBaseQueryClip(moerEntity.tableName) {

    @JvmOverloads
    fun routing(routing: String = ""): EsQueryClip<M, E> {
        this.routing = routing;
        return this;
    }

    fun limit(skip: Long, take: Int): EsQueryClip<M, E> {
        this.search.skip = skip;
        this.search.take = take;
        return this;
    }

    /**
     * 升序
     */
    fun orderByAsc(sortFunc: (M) -> EsColumnName): EsQueryClip<M, E> {
        return this.orderBy(true, sortFunc(this.moerEntity))
    }

    /**
     * 降序
     */
    fun orderByDesc(sortFunc: (M) -> EsColumnName): EsQueryClip<M, E> {
        return this.orderBy(false, sortFunc(this.moerEntity))
    }

    private fun orderBy(asc: Boolean, column: EsColumnName): EsQueryClip<M, E> {
        var order_str = "";
        if (asc) {
            order_str = "asc"
        } else {
            order_str = "desc"
        }

        this.search.sort.add(JsonMap(column.toString() to JsonMap("order" to order_str)))
        return this
    }

    fun should(vararg where: (M) -> WhereData): EsQueryClip<M, E> {
        this.search.query.addShould(*where.map { it.invoke(this.moerEntity) }.toTypedArray())
        return this;
    }

    fun must(vararg where: (M) -> WhereData): EsQueryClip<M, E> {
        this.search.query.addMust(*where.map { it.invoke(this.moerEntity) }.toTypedArray())
        return this;
    }

    fun must_not(vararg where: (M) -> WhereData): EsQueryClip<M, E> {
        this.search.query.addMustNot(*where.map { it.invoke(this.moerEntity) }.toTypedArray())
        return this;
    }

//    fun whereOr(vararg wheres: (M) -> SearchBodyClip): EsQueryClip<M, E> {
//        return whereOr(*wheres.map { it(moerEntity) }.toTypedArray())
//    }
//
//    fun whereOr(vararg wheres: SearchBodyClip): EsQueryClip<M, E> {
//        if (wheres.any() == false) return this;
//
//        return this;
//    }

    fun select(vararg columns: EsColumnName): EsQueryClip<M, E> {
        this.search._source.addAll(columns.map { it.toString() })
        return this;
    }


    fun select(vararg columns: String): EsQueryClip<M, E> {
        this.search._source.addAll(columns)
        return this;
    }

    fun select(column: (M) -> EsColumnName): EsQueryClip<M, E> {
        this.search._source.add(column(this.moerEntity).toString())
        return this;
    }

    fun unSelect(column: (M) -> EsColumnName): EsQueryClip<M, E> {
        return this;
    }


    @JvmOverloads
    fun toList(mapFunc: ((Map<String, Any?>) -> Unit)? = null): MutableList<E> {
        return toList(moerEntity.entityClass, mapFunc)
    }

    @JvmOverloads
    fun toEntity(mapFunc: ((Map<String, Any?>) -> Unit)? = null): E? {
        this.search.take = 1;
        return toList(moerEntity.entityClass, mapFunc).firstOrNull();
    }

    @JvmOverloads
    fun <R> toEntity(clazz: Class<R>, mapFunc: ((Map<String, Any?>) -> Unit)? = null): R? {
        this.search.take = 1;
        return toList(clazz, mapFunc).firstOrNull();
    }


    @JvmOverloads
    fun toListResult(mapFunc: ((Map<String, Any?>) -> Unit)? = null): ListResult<E> {
        return toListResult(this.moerEntity.entityClass, mapFunc);
    }
}

