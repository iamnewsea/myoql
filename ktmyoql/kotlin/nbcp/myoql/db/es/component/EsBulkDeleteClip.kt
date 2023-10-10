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
class EsBulkDeleteClip<M : EsBaseMetaEntity<out Serializable>>(var eserEntity: M)
    : EsBaseBulkDeleteClip(eserEntity.tableName){
    companion object {
        private var logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }


    fun add(id:String): EsBulkDeleteClip<M> {
        super.addId(id)
        return this;
    }

    @JvmOverloads
    fun routing(routing:String = ""): EsBulkDeleteClip<M> {
        this.withRouting(routing)
        return this;
    }

    @JvmOverloads
    fun pipeline(pipeline:String = ""): EsBulkDeleteClip<M> {
        this.withPipeLine(pipeline)
        return this;
    }

    fun refresh(refresh: EsPutRefreshEnum): EsBulkDeleteClip<M> {
        this.withRefresh(refresh)
        return this;
    }
}