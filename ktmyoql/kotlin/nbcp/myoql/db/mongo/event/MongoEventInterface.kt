package nbcp.myoql.db.mongo.event;


import nbcp.myoql.db.mongo.*;
import nbcp.myoql.db.comm.EventResult
import nbcp.myoql.db.mongo.component.MongoBaseUpdateClip


interface IMongoEntityQuery {
    fun beforeQuery(query: MongoBaseQueryClip): EventResult

    fun query(query: MongoBaseQueryClip, eventData: EventResult)
}


interface IMongoEntityInsert {
    fun beforeInsert(insert: MongoBaseInsertClip): EventResult

    fun insert(insert: MongoBaseInsertClip, eventData: EventResult)
}

/**
 * 实体Update接口，标记 DbEntityUpdate 注解的类使用。
 */
interface IMongoEntityUpdate {
    fun beforeUpdate(update: MongoBaseUpdateClip): EventResult

    fun update(update: MongoBaseUpdateClip, eventData: EventResult)
}

/**
 * 实体 Delete 接口，标记 DbEntityDelete 注解的类使用。
 */
interface IMongoEntityDelete {
    fun beforeDelete(delete: MongoDeleteClip<*>): EventResult

    fun delete(delete: MongoDeleteClip<*>, eventData: EventResult)
}


interface IMongoEntityAggregate {
    fun beforeAggregate(query: MongoAggregateClip<*, out Any>): EventResult

    fun aggregate(query: MongoAggregateClip<*, out Any>, eventData: EventResult)
}
///**
// * 动态库使用
// */
//interface IMongoDataSource {
//    fun run(collection: String, isRead: Boolean): MongoTemplate?
//}

/**
 * 变表使用
 */
interface IMongoCollectionVarName {
    fun run(String: String): String
}