package nbcp.db.mongo


import nbcp.base.extend.*
import nbcp.base.line_break
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
 * MongoDelete
 */
class MongoInsertClip<M : MongoBaseEntity<E>, E : IMongoDocument>(var moerEntity: M) : MongoClipBase(moerEntity.tableName), IMongoWhereable {
    val whereData = mutableListOf<Criteria>()

    companion object {
        private var logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    private var entities = mutableListOf<E>()

    fun insert(entity: E): MongoInsertClip<M, E> {
        this.entities.add(entity);
        return this;
    }

    fun exec(): Int {
        db.affectRowCount = -1;
        var ret = 0;
        try {
            mongoTemplate.insertAll(entities)
            ret = entities.size;
            db.affectRowCount = entities.size
            return db.affectRowCount
        } catch (e: Exception) {
            ret = -1;
            throw e;
        } finally {
            logger.InfoError(ret < 0) { "insert ${entities.size} enities：[" + this.collectionName + "] " + ",result:${ret}" };
        }

        return ret;
    }
}