package nbcp.db.mongo.component

import nbcp.base.extend.InfoError
import nbcp.db.db
import nbcp.db.mongo.IMongoDocument
import nbcp.db.mongo.IMongoWhereable
import nbcp.db.mongo.MongoClipBase
import nbcp.db.mongo.MongoInsertClip
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory
import java.lang.Exception
import java.time.LocalDateTime

open class MongoBaseInsertClip(tableName: String) : MongoClipBase(tableName), IMongoWhereable {
    companion object {
        private val logger by lazy {
            return@lazy LoggerFactory.getLogger(this::class.java)
        }
    }

    var entities = mutableListOf<Any>()

    fun addEntity(entity: Any) {
        if (entity is IMongoDocument) {
            if (entity.id.isEmpty()) {
                entity.id = ObjectId().toString()
            }
            entity.createAt = LocalDateTime.now()
        }
        this.entities.add(entity)
    }

    fun exec(): Int {
        db.affectRowCount = -1;
        var ret = 0;

        var settingResult = db.mongo.mongoEvents.onInserting(this)
        if (settingResult.any { it.second.result == false }) {
            return 0;
        }

        try {
            mongoTemplate.insertAll(entities)

            settingResult.forEach {
                it.first.insert(this, it.second)
            }

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