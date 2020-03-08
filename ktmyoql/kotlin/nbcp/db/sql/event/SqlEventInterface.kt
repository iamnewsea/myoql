package nbcp.db.sql

import nbcp.db.DbEntityEventResult
import nbcp.db.mongo.component.MongoBaseInsertClip

interface ISqlEntityInsert {
    fun beforeInsert(update: SqlInsertClip<*,*>): DbEntityEventResult?

    fun insert(update: SqlInsertClip<*,*>, eventData: DbEntityEventResult?)
}


/**
 * 实体Update接口，标记 DbEntityUpdate 注解的类使用。
 */
interface ISqlEntityUpdate {
    fun beforeUpdate(update: SqlUpdateClip<*,*>): DbEntityEventResult?

    fun update(update: SqlUpdateClip<*,*>, eventData: DbEntityEventResult?)
}

/**
 * 实体 Delete 接口，标记 DbEntityDelete 注解的类使用。
 */
interface ISqlEntityDelete {
    fun beforeDelete(delete: SqlDeleteClip<*,*>): DbEntityEventResult?

    fun delete(delete: SqlDeleteClip<*,*>, eventData: DbEntityEventResult?)
}