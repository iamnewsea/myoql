package nbcp.db.mongo

import com.mongodb.client.result.DeleteResult
import com.mongodb.client.result.UpdateResult
import nbcp.comm.*
import nbcp.db.db
import nbcp.utils.SpringUtil
import org.bson.Document
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update


    private val mongoLog by lazy {
        return@lazy SpringUtil.getBean<MongoCollectionLogProperties>()
    }

    fun Logger.logFind( error: Exception?, collectionName: String, queryJson: String, result: Document?) {
        this.log(error, collectionName, queryJson, result?.ToJson() ?: "", mongoLog::getQueryLog)
    }

    fun Logger.logFind(error: Exception?, collectionName: String, queryJson: Query, result: JsonMap) {
        this.log(error, collectionName, queryJson.queryObject.ToJson(), result.ToJson(), mongoLog::getQueryLog)
    }

    fun Logger.logFind(error: Exception?, collectionName: String, getMsg: () -> String) {
        this.log(error, collectionName, mongoLog::getQueryLog, getMsg);
    }

    fun Logger.logInsert(error: java.lang.Exception?, collectionName: String, entities: MutableList<Any>) {
        this.log(error, collectionName, entities.ToJson(), "", mongoLog::getInsertLog)
    }

    private fun Logger.log(
        error: Exception?, collectionName: String, queryJson: String, result: String, op: (String) -> Boolean
    ) {
        var getMsg: () -> String = getMsg@{
            """[${collectionName}] ${queryJson}
${if (result.HasValue) ("[result] " + result + "\n") else ""}[耗时] ${db.executeTime}"""
        }

        if (error != null) {
            this.error(getMsg())
            this.error(error.message, error);
            return;
        }

        if (this.scopeInfoLevel) {
            this.info(getMsg())
            return;
        }

        //如果指定了输出Sql
        if (op(collectionName)) {
            this.Important(getMsg())
        }
    }


    private fun Logger.log(
        error: Exception?, collectionName: String, op: (String) -> Boolean,
        getMsg: (() -> String)
    ) {
        if (error != null) {
            this.error(getMsg())
            this.error(error.message, error);
            return;
        }

        if (this.scopeInfoLevel) {
            this.info(getMsg())
            return;
        }

        //如果指定了输出Sql
        if (op(collectionName)) {
            this.Important(getMsg())
        }
    }

    fun Logger.logUpdate(error: Exception?, collectionName: String, query: Query, update: Update, result: UpdateResult?) {
        this.log(error, collectionName, mongoLog::getUpdateLog, {
            return@log """[update] ${collectionName}
[where] ${query.queryObject.ToJson()}
[set] ${update.ToJson()}
[result] ${result?.ToJson()}
[耗时] ${db.executeTime}"""
        })
    }

    fun Logger.logDelete(error: Exception?, collectionName: String, query: Query, result: DeleteResult?) {
        this.log(error, collectionName, mongoLog::getDeleteLog, {
            return@log "delete:[" + collectionName + "] " + query.queryObject.ToJson() + ",result:${result?.ToJson()}"
        })

    }
