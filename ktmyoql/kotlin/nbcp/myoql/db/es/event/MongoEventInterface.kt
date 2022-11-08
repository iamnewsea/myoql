package nbcp.myoql.db.es.event


import nbcp.myoql.db.comm.EventResult
import nbcp.myoql.db.es.component.EsBaseDeleteClip
import nbcp.myoql.db.es.component.EsBaseInsertClip
import nbcp.myoql.db.es.component.EsBaseQueryClip
import nbcp.myoql.db.es.component.EsBaseUpdateClip
import org.elasticsearch.client.RestHighLevelClient

interface IEsEntityQuery {
    fun beforeQuery(query: EsBaseQueryClip): EventResult

    fun query(query: EsBaseQueryClip, eventData: EventResult)
}

interface IEsEntityInsert {
    fun beforeInsert(insert: EsBaseInsertClip): EventResult

    fun insert(insert: EsBaseInsertClip, eventData: EventResult)
}

/**
 * 实体Update接口，标记 DbEntityUpdate 注解的类使用。
 */
interface IEsEntityUpdate {
    fun beforeUpdate(update: EsBaseUpdateClip): EventResult

    fun update(update: EsBaseUpdateClip, eventData: EventResult)
}

/**
 * 实体 Delete 接口，标记 DbEntityDelete 注解的类使用。
 */
interface IEsEntityDelete {
    fun beforeDelete(delete: EsBaseDeleteClip): EventResult

    fun delete(delete: EsBaseDeleteClip, eventData: EventResult)
}


interface IEsDataSource {
    fun run(collection: String, isRead: Boolean): RestHighLevelClient?
}