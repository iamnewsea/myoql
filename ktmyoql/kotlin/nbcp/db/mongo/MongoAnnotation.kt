package nbcp.db.mongo

import nbcp.db.DbEntityEventResult

/**
 * 实体Update接口，标记 DbEntityUpdate 注解的类使用。
 */
interface IMongoEntityUpdate {
    fun beforeUpdate(update: MongoUpdateClip<*>): DbEntityEventResult?

    fun update(update: MongoUpdateClip<*>, eventData: DbEntityEventResult?)
}

/**
 * 实体 Delete 接口，标记 DbEntityDelete 注解的类使用。
 */
interface IMongoEntityDelete {
    fun beforeDelete(delete: MongoDeleteClip<*>): DbEntityEventResult?

    fun delete(delete: MongoDeleteClip<*>, eventData: DbEntityEventResult?)
}