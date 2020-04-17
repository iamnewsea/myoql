package nbcp.db.es


import nbcp.comm.*
import nbcp.db.*
import nbcp.db.es.*
import org.slf4j.LoggerFactory
import java.lang.Exception
import java.time.LocalDateTime

/**
 * Created by udi on 17-4-17.
 */

/**
 * EsDelete
 */
class EsDeleteClip<M : EsBaseEntity<out IEsDocument>>(var eserEntity: M) : EsBaseDeleteClip(eserEntity.tableName), IEsWhereable {
    companion object {
        private var logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }


    fun add(id:String): EsDeleteClip<M> {
        super.addId(id)
        return this;
    }

    fun routing(routing:String = ""):  EsDeleteClip<M> {
        this.withRouting(routing)
        return this;
    }

    fun pipeline(pipeline:String = ""): EsDeleteClip<M> {
        this.withPipeLine(pipeline)
        return this;
    }

    fun refresh(refresh:EsPutRefreshEnum): EsDeleteClip<M> {
        this.withRefresh(refresh)
        return this;
    }
}