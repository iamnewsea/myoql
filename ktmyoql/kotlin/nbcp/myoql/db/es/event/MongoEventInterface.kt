package nbcp.myoql.db.es.event


import nbcp.myoql.db.comm.EventResult
import nbcp.myoql.db.es.component.EsBaseBulkDeleteClip
import nbcp.myoql.db.es.component.EsBaseBulkInsertClip
import nbcp.myoql.db.es.component.EsBaseQueryClip
import nbcp.myoql.db.es.component.EsBaseBulkUpdateClip
import org.elasticsearch.client.RestClient

interface IEsEntityQuery {
    fun beforeQuery(query: EsBaseQueryClip): EventResult

    fun query(query: EsBaseQueryClip, eventData: EventResult)
}

interface IEsEntityInsert {
    fun beforeInsert(insert: EsBaseBulkInsertClip): EventResult

    fun insert(insert: EsBaseBulkInsertClip, eventData: EventResult)
}

/**
 * 实体Update接口，标记 DbEntityUpdate 注解的类使用。
 */
interface IEsEntityUpdate {
    fun beforeUpdate(update: EsBaseBulkUpdateClip): EventResult

    fun update(update: EsBaseBulkUpdateClip, eventData: EventResult)
}

/**
 * 实体 Delete 接口，标记 DbEntityDelete 注解的类使用。
 */
interface IEsEntityDelete {
    fun beforeDelete(delete: EsBaseBulkDeleteClip): EventResult

    fun delete(delete: EsBaseBulkDeleteClip, eventData: EventResult)
}


interface IEsDataSource {
    fun run(collection: String, isRead: Boolean): RestClient?
}