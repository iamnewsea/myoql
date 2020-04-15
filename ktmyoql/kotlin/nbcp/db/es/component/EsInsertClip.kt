package nbcp.db.es


import nbcp.comm.*

import nbcp.db.db
import org.slf4j.LoggerFactory
import java.lang.Exception

/**
 * Created by udi on 17-4-17.
 */


/**
 * EsInsert
 */
class EsInsertClip<M : EsBaseEntity<E>, E : IEsDocument>(var moerEntity: M) : EsBaseInsertClip(moerEntity.tableName)  {

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
}