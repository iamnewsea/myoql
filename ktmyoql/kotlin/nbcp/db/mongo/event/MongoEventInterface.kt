package nbcp.db.mongo.event;

import nbcp.db.EventChain
import nbcp.db.mongo.*;
import nbcp.db.EventResult
import org.springframework.data.mongodb.core.MongoTemplate
import javax.sql.DataSource


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
    fun beforeUpdate(update: MongoBaseUpdateClip, chain: EventChain): EventResult

    fun update(update: MongoBaseUpdateClip, chain: EventChain, eventData: EventResult)
}

/**
 * 实体 Delete 接口，标记 DbEntityDelete 注解的类使用。
 */
interface IMongoEntityDelete {
    fun beforeDelete(delete: MongoDeleteClip<*>, chain: EventChain): EventResult

    fun delete(delete: MongoDeleteClip<*>, chain: EventChain, eventData: EventResult)
}

interface IMongoDataSource {
    fun run(collection: String, isRead: Boolean): MongoTemplate?
}