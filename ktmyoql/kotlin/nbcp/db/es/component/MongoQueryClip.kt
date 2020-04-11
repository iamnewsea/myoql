package nbcp.db.es

import nbcp.comm.*
import nbcp.base.extend.*

import nbcp.base.utils.Md5Util
import nbcp.base.utils.MyUtil
import nbcp.db.db
import nbcp.db.es.*
import org.slf4j.LoggerFactory
import java.lang.Exception
import java.lang.RuntimeException



/**
 * EsQuery
 */
class EsQueryClip<M : EsBaseEntity<E>, E : IEsDocument>(var moerEntity: M) : EsBaseQueryClip(moerEntity.tableName) {


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

    private fun orderBy(asc: Boolean, field: EsColumnName): EsQueryClip<M, E> {
        var sortName = field.toString()
        if (sortName == "id") {
            sortName = "_id"
        } else if (sortName.endsWith(".id")) {
            sortName = sortName.slice(0..sortName.length - 3) + "._id";
        }

        this.search.sortObject.put(sortName, if (asc) 1 else -1)
        return this;
    }

    fun where(whereData: SearchBodyClip): EsQueryClip<M, E> {
        this.search = whereData
        return this;
    }


    fun where(where: (M) -> SearchBodyClip): EsQueryClip<M, E> {
        this.search =  where(this.moerEntity)
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


    fun toList(mapFunc: ((Map<String,Any?>) -> Unit)? = null): MutableList<E> {
        return toList(moerEntity.entityClass, mapFunc)
    }

    fun toEntity(mapFunc: ((Map<String,Any?>) -> Unit)? = null): E? {
        this.search.take = 1;
        return toList(moerEntity.entityClass, mapFunc).firstOrNull();
    }

    fun <R> toEntity(clazz: Class<R>, mapFunc: ((Map<String,Any?>) -> Unit)? = null): R? {
        this.search.take = 1;
        return toList(clazz, mapFunc).firstOrNull();
    }


    fun toListResult(mapFunc: ((Map<String,Any?>) -> Unit)? = null): ListResult<E> {
        return toListResult(this.moerEntity.entityClass, mapFunc);
    }
}

