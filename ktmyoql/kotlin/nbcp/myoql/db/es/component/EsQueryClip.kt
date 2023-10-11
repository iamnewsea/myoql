package nbcp.myoql.db.es.component

import nbcp.base.comm.JsonMap
import nbcp.base.comm.ListResult
import nbcp.myoql.db.es.base.EsColumnName
import java.io.Serializable

/**
 * EsQuery
 * https://www.elastic.co/guide/en/elasticsearch/reference/7.6/search.html
 */
class EsQueryClip<M : EsBaseMetaEntity<E>, E : Serializable>(var esEntity: M) :
    EsBaseQueryClip(esEntity.tableName) {

    @JvmOverloads
    fun routing(routing: String = ""): EsQueryClip<M, E> {
        this.routing = routing;
        return this;
    }

    fun limit(skip: Long, take: Int): EsQueryClip<M, E> {
        setLimit(skip, take)
        return this;
    }

    /**
     * 升序
     */
    fun orderByAsc(sortFunc: (M) -> EsColumnName): EsQueryClip<M, E> {
        setOrderByAsc(sortFunc(this.esEntity))
        return this;
    }

    /**
     * 降序
     */
    fun orderByDesc(sortFunc: (M) -> EsColumnName): EsQueryClip<M, E> {
        setOrderByDesc(sortFunc(this.esEntity))
        return this
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
        setShould(*where.map { it.invoke(this.esEntity) }.toTypedArray())
        return this;
    }

    fun must(vararg where: (M) -> WhereData): EsQueryClip<M, E> {
        setMust(*where.map { it.invoke(this.esEntity) }.toTypedArray())
        return this;
    }

    fun mustNot(vararg where: (M) -> WhereData): EsQueryClip<M, E> {
        setMustNot(*where.map { it.invoke(this.esEntity) }.toTypedArray())
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
        this.search._source.add(column(this.esEntity).toString())
        return this;
    }

    fun unSelect(column: (M) -> EsColumnName): EsQueryClip<M, E> {
        return this;
    }


    @JvmOverloads
    fun toList(mapFunc: ((Map<String, Any?>) -> Unit)? = null): MutableList<E> {
        return toList(esEntity.entityClass, mapFunc)
    }

    @JvmOverloads
    fun toEntity(mapFunc: ((Map<String, Any?>) -> Unit)? = null): E? {
        this.search.take = 1;
        return toList(esEntity.entityClass, mapFunc).firstOrNull();
    }

    @JvmOverloads
    fun <R> toEntity(type: Class<R>, mapFunc: ((Map<String, Any?>) -> Unit)? = null): R? {
        this.search.take = 1;
        return toList(type, mapFunc).firstOrNull();
    }


    @JvmOverloads
    fun toListResult(mapFunc: ((Map<String, Any?>) -> Unit)? = null): ListResult<E> {
        return toListResult(this.esEntity.entityClass, mapFunc);
    }
}

