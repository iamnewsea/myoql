package nbcp.db.es


import org.slf4j.LoggerFactory

/**
 * Created by udi on 17-4-17.
 */


/**
 * EsInsert
 */
class EsInsertClip<M : EsBaseMetaEntity<E>, E : IEsDocument>(var moerEntity: M)
    : EsBaseInsertClip(moerEntity.tableName)  {

    companion object {
        private var logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    fun add(entity: E): EsInsertClip<M, E> {
        super.addEntity(entity)
        return this;
    }

    fun routing(routing:String = ""):  EsInsertClip<M, E> {
        this.withRouting(routing)
        return this;
    }

    fun pipeline(pipeline:String = ""): EsInsertClip<M, E> {
        this.withPipeLine(pipeline)
        return this;
    }

    fun refresh(refresh:EsPutRefreshEnum): EsInsertClip<M, E> {
        this.withRefresh(refresh)
        return this;
    }
}