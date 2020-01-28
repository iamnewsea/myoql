package nbcp.db.mongo

import nbcp.db.DbEntityEventResult
import nbcp.db.mongo.component.MongoBaseUpdateClip

/**
 * 实体Update接口，标记 DbEntityUpdate 注解的类使用。
 */
interface IMongoEntityUpdate {
    fun beforeUpdate(update: MongoBaseUpdateClip): DbEntityEventResult

    fun update(update: MongoBaseUpdateClip, eventData: DbEntityEventResult)
}

/**
 * 实体 Delete 接口，标记 DbEntityDelete 注解的类使用。
 */
interface IMongoEntityDelete {
    fun beforeDelete(delete: MongoDeleteClip<*>): DbEntityEventResult

    fun delete(delete: MongoDeleteClip<*>, eventData: DbEntityEventResult)
}