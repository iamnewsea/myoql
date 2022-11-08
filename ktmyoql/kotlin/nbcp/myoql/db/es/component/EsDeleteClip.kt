package nbcp.myoql.db.es.component


import nbcp.myoql.db.es.enums.EsPutRefreshEnum
import org.slf4j.LoggerFactory
import java.io.Serializable

/**
 * Created by udi on 17-4-17.
 */

/**
 * EsDelete
 */
class EsDeleteClip<M : EsBaseMetaEntity<out Serializable>>(var eserEntity: M) : EsBaseDeleteClip(eserEntity.tableName),
    IEsWhereable {
    companion object {
        private var logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }


    fun add(id:String): EsDeleteClip<M> {
        super.addId(id)
        return this;
    }

    @JvmOverloads
    fun routing(routing:String = ""): EsDeleteClip<M> {
        this.withRouting(routing)
        return this;
    }

    @JvmOverloads
    fun pipeline(pipeline:String = ""): EsDeleteClip<M> {
        this.withPipeLine(pipeline)
        return this;
    }

    fun refresh(refresh: EsPutRefreshEnum): EsDeleteClip<M> {
        this.withRefresh(refresh)
        return this;
    }
}