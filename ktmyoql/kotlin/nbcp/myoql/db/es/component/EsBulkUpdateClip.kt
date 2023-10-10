package nbcp.myoql.db.es

import nbcp.myoql.db.es.component.EsBaseMetaEntity
import nbcp.myoql.db.es.component.EsBaseBulkUpdateClip
import nbcp.myoql.db.es.enums.EsPutRefreshEnum
import java.io.Serializable

/**
 * Created by udi on 17-4-7.
 */

//根据Id，更新Es的一个键。

/**
 * EsUpdate
 * https://www.elastic.co/guide/en/elasticsearch/reference/7.6/docs-update-by-query.html
 * https://www.elastic.co/guide/cn/elasticsearch/guide/current/partial-updates.html
 */
class EsBulkUpdateClip<M : EsBaseMetaEntity<E>,E : Serializable>(var moerEntity: M)
    : EsBaseBulkUpdateClip(moerEntity.tableName) {

    fun add(entity: E): EsBulkUpdateClip<M, E> {
        super.addEntity(entity)
        return this;
    }

    @JvmOverloads
    fun routing(routing:String = ""):  EsBulkUpdateClip<M, E> {
        this.withRouting(routing)
        return this;
    }

    @JvmOverloads
    fun pipeline(pipeline:String = ""): EsBulkUpdateClip<M, E> {
        this.withPipeLine(pipeline)
        return this;
    }

    fun refresh(refresh: EsPutRefreshEnum): EsBulkUpdateClip<M, E> {
        this.withRefresh(refresh)
        return this;
    }

}

