package nbcp.db.mongo


import nbcp.base.extend.*

import nbcp.db.db
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import nbcp.db.mongo.*
import org.slf4j.LoggerFactory
import java.lang.Exception

/**
 * Created by udi on 17-4-17.
 */


/**
 * MongoInsert
 */
class MongoInsertClip<M : MongoBaseEntity<E>, E : IMongoDocument>(var moerEntity: M) : MongoBaseInsertClip(moerEntity.tableName)  {

    companion object {
        private var logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    fun add(entity: E): MongoInsertClip<M, E> {
        super.addEntity(entity)
        return this;
    }
}