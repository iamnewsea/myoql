package nbcp.db.sql

import nbcp.db.DbEntityEventResult
import nbcp.db.mongo.*

/**
 * 处理 Insert 的 Bean 接口。
 */
interface ISqlEntityInsert {
    fun beforeInsert(update: SqlInsertClip<*,*>): DbEntityEventResult?

    fun insert(update: SqlInsertClip<*,*>, eventData: DbEntityEventResult?)
}


/**
 * 处理 Update 的 Bean 接口。
 */
interface ISqlEntityUpdate {
    fun beforeUpdate(update: SqlUpdateClip<*,*>): DbEntityEventResult?

    fun update(update: SqlUpdateClip<*,*>, eventData: DbEntityEventResult?)
}

/**
 * 处理 Delete 的 Bean 接口。
 */
interface ISqlEntityDelete {
    fun beforeDelete(delete: SqlDeleteClip<*,*>): DbEntityEventResult?

    fun delete(delete: SqlDeleteClip<*,*>, eventData: DbEntityEventResult?)
}