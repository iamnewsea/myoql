package nbcp.db.sql.event

import nbcp.db.sql.*;
import nbcp.db.EventResult
import javax.sql.DataSource

/**
 * 处理 select 的 Bean 接口。
 */
interface ISqlEntitySelect {
    fun beforeSelect(select: SqlBaseQueryClip): EventResult?

    fun select(select: SqlBaseQueryClip, eventData: EventResult?, result: List<MutableMap<String, Any?>>)
}

/**
 * 处理 Insert 的 Bean 接口。
 */
interface ISqlEntityInsert {
    fun beforeInsert(insert: SqlInsertClip<*, *>): EventResult?

    fun insert(insert: SqlInsertClip<*, *>, eventData: EventResult?)
}


/**
 * 处理 Update 的 Bean 接口。
 */
interface ISqlEntityUpdate {
    fun beforeUpdate(update: SqlUpdateClip<*, *>): EventResult?

    fun update(update: SqlUpdateClip<*, *>, eventData: EventResult?)
}

/**
 * 处理 Delete 的 Bean 接口。
 */
interface ISqlEntityDelete {
    fun beforeDelete(delete: SqlDeleteClip<*, *>): EventResult?

    fun delete(delete: SqlDeleteClip<*, *>, eventData: EventResult?)
}

interface ISqlDataSource {
    fun run(tableName: String, isRead: Boolean): DataSource?
}
