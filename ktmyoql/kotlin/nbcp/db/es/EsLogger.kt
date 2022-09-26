package nbcp.db.es.logger


import nbcp.comm.*
import nbcp.db.db
import nbcp.db.es.*
import nbcp.db.es.tool.EsTableLogProperties
import nbcp.utils.SpringUtil
import org.elasticsearch.client.Request
import org.elasticsearch.client.Response
import org.slf4j.Logger
import org.slf4j.LoggerFactory


val esLog by lazy {
    return@lazy SpringUtil.getBean<EsTableLogProperties>()
}

inline fun Logger.logGet(error: Exception?, esName: String, request: Request, response: String) {
    log(error, esName, request, response, esLog::getQueryLog)
}

inline fun Logger.logDelete(error: Exception?, esName: String, request: Request, response: String) {
    log(error, esName, request, response, esLog::getDeleteLog)
}

inline fun Logger.logPost(error: Exception?, esName: String, request: Request, response: String) {
    log(error, esName, request, response, esLog::getInsertLog)
}

inline fun Logger.logPut(error: Exception?, esName: String, request: Request, response: String) {
    log(error, esName, request, response, esLog::getUpdateLog)
}


inline fun Logger.log(error: Exception?, esName: String, request: Request, response: String, op: (String) -> Boolean) {
    val getMsg: () -> String = getMsg@{
        """[${request.method}] ${request.endpoint} 
[body] ${request.entity.content.readContentString()} 
[result] ${response}
[耗时] ${db.executeTime}"""
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
    if (op(esName)) {
        this.Important(getMsg())
    }
}
