package nbcp.db.es

import nbcp.comm.*

import org.slf4j.LoggerFactory


/**
 * EsQuery
 */
class EsQueryClip<M : EsBaseEntity<E>, E : IEsDocument>(var moerEntity: M) : EsBaseQueryClip(moerEntity.tableName) {


    fun limit(skip: Long, take: Int): EsQueryClip<M, E> {
        this.search.from = skip;
        this.search.size = take;
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

    fun where(whereData: SearchBodyClip): EsQueryClip<M, E> {
        this.search = whereData
        return this;
    }


    fun where(where: (M) -> SearchBodyClip): EsQueryClip<M, E> {
        this.search = where(this.moerEntity)
        return this;
    }

    fun whereOr(vararg wheres: (M) -> SearchBodyClip): EsQueryClip<M, E> {
        return whereOr(*wheres.map { it(moerEntity) }.toTypedArray())
    }

    fun whereOr(vararg wheres: SearchBodyClip): EsQueryClip<M, E> {
        if (wheres.any() == false) return this;

        return this;
    }

    fun whereIf(whereIf: Boolean, whereData: ((M) -> SearchBodyClip)): EsQueryClip<M, E> {
        if (whereIf == false) return this;


        return this;
    }

    fun select(vararg columns: EsColumnName): EsQueryClip<M, E> {

        return this;
    }


    fun select(vararg columns: String): EsQueryClip<M, E> {

        return this;
    }

    fun select(column: (M) -> EsColumnName): EsQueryClip<M, E> {

        return this;
    }

    fun unSelect(column: (M) -> EsColumnName): EsQueryClip<M, E> {

        return this;
    }

    companion object {
        private val logger by lazy {
            return@lazy LoggerFactory.getLogger(this::class.java)
        }
    }


    fun toList(mapFunc: ((Map<String, Any?>) -> Unit)? = null): MutableList<E> {
        return toList(moerEntity.entityClass, mapFunc)
    }

    fun toEntity(mapFunc: ((Map<String, Any?>) -> Unit)? = null): E? {
        this.search.size = 1;
        return toList(moerEntity.entityClass, mapFunc).firstOrNull();
    }

    fun <R> toEntity(clazz: Class<R>, mapFunc: ((Map<String, Any?>) -> Unit)? = null): R? {
        this.search.size = 1;
        return toList(clazz, mapFunc).firstOrNull();
    }


    fun toListResult(mapFunc: ((Map<String, Any?>) -> Unit)? = null): ListResult<E> {
        return toListResult(this.moerEntity.entityClass, mapFunc);
    }
}

