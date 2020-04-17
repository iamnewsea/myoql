package nbcp.db.es

import nbcp.db.DbEntityEventResult
import nbcp.db.es.*

interface IEsEntityInsert {
    fun beforeInsert(insert: EsBaseInsertClip): DbEntityEventResult

    fun insert(insert: EsBaseInsertClip, eventData: DbEntityEventResult)
}

/**
 * 实体Update接口，标记 DbEntityUpdate 注解的类使用。
 */
interface IEsEntityUpdate {
    fun beforeUpdate(update: EsBaseUpdateClip): DbEntityEventResult

    fun update(update: EsBaseUpdateClip, eventData: DbEntityEventResult)
}

/**
 * 实体 Delete 接口，标记 DbEntityDelete 注解的类使用。
 */
interface IEsEntityDelete {
    fun beforeDelete(delete: EsBaseDeleteClip): DbEntityEventResult

    fun delete(delete: EsBaseDeleteClip, eventData: DbEntityEventResult)
}