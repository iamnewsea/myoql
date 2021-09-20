package nbcp.db.mongo

import com.mongodb.client.result.DeleteResult
import com.mongodb.client.result.UpdateResult
import nbcp.comm.*
import nbcp.db.db
import nbcp.utils.SpringUtil
import org.bson.Document
import org.slf4j.LoggerFactory
import org.springframework.boot.logging.LogLevel
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update

object MongoLogger {
    private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    private val mongoLog by lazy {
        return@lazy SpringUtil.getBean<MongoCollectionLogProperties>()
    }


    fun logFind(error: Exception?, collectionName: String, queryJson: String, result: Document?) {
        log(error, collectionName, queryJson, result?.ToJson() ?: "", mongoLog::getFindLog)
    }

    fun logFind(error: Exception?, collectionName: String, queryJson: JsonMap, result: JsonMap) {
        log(error, collectionName, queryJson.ToJson(), result.ToJson(), mongoLog::getFindLog)
    }

    fun logFind(error: Exception?, collectionName: String, getMsg: () -> String) {
        log(error, collectionName, mongoLog::getFindLog, getMsg);
    }

    fun logInsert(error: java.lang.Exception?, collectionName: String, entities: MutableList<Any>) {
        log(error, collectionName, entities.ToJson(), "", mongoLog::getInsertLog)
    }

    fun log(
        error: Exception?, collectionName: String, queryJson: String, result: String, op: (String) -> Boolean
    ) {
        var getMsg: () -> String = getMsg@{
            """[${collectionName}] ${queryJson}
${if (result.HasValue) ("[result] " + result + "\n") else ""}[耗时] ${db.executeTime}"""
        }

        if (error != null) {
            logger.error(getMsg())
            logger.error(error.message, error);
            return;
        }

        if (logger.scopeInfoLevel) {
            logger.info(getMsg())
            return;
        }

        //如果指定了输出Sql
        if (op(collectionName)) {
            usingScope(LogLevel.INFO) {
                logger.info(getMsg())
            }
        }
    }


    fun log(
        error: Exception?, collectionName: String, op: (String) -> Boolean,
        getMsg: (() -> String)
    ) {
        if (error != null) {
            logger.error(getMsg())
            logger.error(error.message, error);
            return;
        }

        if (logger.scopeInfoLevel) {
            logger.info(getMsg())
            return;
        }

        //如果指定了输出Sql
        if (op(collectionName)) {
            usingScope(LogLevel.INFO) {
                logger.info(getMsg())
            }
        }
    }

    fun logUpdate(error: Exception?, collectionName: String, query: Query, update: Update, result: UpdateResult?) {
        log(error, collectionName, mongoLog::getUpdateLog, {
            return@log """[update] ${collectionName}
[where] ${query.queryObject.toJson()}
[set] ${update.ToJson()}
[result] ${result?.ToJson()}
[耗时] ${db.executeTime}"""
        })
    }

    fun logDelete(error: Exception?, collectionName: String, query: Query, result: DeleteResult?) {
        log(error, collectionName, mongoLog::getRemoveLog, {
            return@log "delete:[" + collectionName + "] " + query.queryObject.toJson() + ",result:${result?.ToJson()}"
        })

    }
}