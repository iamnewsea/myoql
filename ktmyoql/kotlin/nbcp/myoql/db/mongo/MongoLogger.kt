package nbcp.myoql.db.mongo.logger

import com.mongodb.client.result.DeleteResult
import com.mongodb.client.result.UpdateResult
import nbcp.base.enums.JsonStyleScopeEnum
import nbcp.base.extend.*
import nbcp.base.utils.SpringUtil
import nbcp.myoql.db.db
import nbcp.myoql.db.mongo.MongoCollectionLogProperties
import org.bson.Document
import org.slf4j.Logger
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update


val mongoLog by lazy {
    return@lazy SpringUtil.getBean<MongoCollectionLogProperties>()
}

inline fun Logger.logFind(error: Exception?, collectionName: String, queryJson: String, result: Document?) {
    this.myoqlLog(error, collectionName, queryJson, result?.ToJson() ?: "", mongoLog::getQueryLog)
}

inline fun Logger.logFind(error: Exception?, collectionName: String, queryJson: Query, result: Map<String,Any?>) {
    this.myoqlLog(error, collectionName, queryJson.queryObject.ToJson(), result.ToJson(), mongoLog::getQueryLog)
}

inline fun Logger.logFind(error: Exception?, collectionName: String, getMsg: () -> String) {
    this.myoqlLog(error, collectionName, mongoLog::getQueryLog, getMsg);
}

inline fun Logger.logInsert(error: java.lang.Exception?, collectionName: String, entities: MutableList<Any>) {
    this.myoqlLog(error, collectionName, entities.ToJson(), "", mongoLog::getInsertLog)
}

inline fun Logger.myoqlLog(
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


inline fun Logger.myoqlLog(
    error: Exception?, collectionName: String, op: (String) -> Boolean,
    getMsg: (() -> String)
) {
    usingScope(JsonStyleScopeEnum.WithNull) {
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
}

inline fun Logger.logUpdate(
    error: Exception?,
    collectionName: String,
    query: Query,
    update: Update,
    result: UpdateResult?
) {
    this.myoqlLog(error, collectionName, mongoLog::getUpdateLog, {
        return@myoqlLog """[update] ${collectionName}
[where] ${query.queryObject.ToJson()}
[set] ${update.ToJson()}
[result] ${result?.ToJson()}
[耗时] ${db.executeTime}"""
    })
}

inline fun Logger.logDelete(error: Exception?, collectionName: String, query: Query, result: DeleteResult?) {
    this.myoqlLog(error, collectionName, mongoLog::getDeleteLog, {
        return@myoqlLog "delete:[" + collectionName + "] " + query.queryObject.ToJson() + ",result:${result?.ToJson()}"
    })

}
