package nbcp.db.es


import nbcp.comm.*
import nbcp.db.db
import nbcp.db.es.tool.EsTableLogProperties
import nbcp.utils.SpringUtil
import org.elasticsearch.client.Request
import org.elasticsearch.client.Response
import org.slf4j.LoggerFactory


object EsLogger {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val esLog by lazy {
        return@lazy SpringUtil.getBean<EsTableLogProperties>()
    }

 @JvmStatic fun  logGet(error: Exception?, esName: String, request: Request, response: String) {
        log(error, esName, request, response, esLog::getQueryLog)
    }

 @JvmStatic fun  logDelete(error: Exception?, esName: String, request: Request, response: String) {
        log(error, esName, request, response, esLog::getDeleteLog)
    }

 @JvmStatic fun  logPost(error: Exception?, esName: String, request: Request, response: String) {
        log(error, esName, request, response, esLog::getInsertLog)
    }

 @JvmStatic fun  logPut(error: Exception?, esName: String, request: Request, response: String) {
        log(error, esName, request, response, esLog::getUpdateLog)
    }


 @JvmStatic fun  log(error: Exception?, esName: String, request: Request, response: String, op: (String) -> Boolean) {
        val getMsg: () -> String = getMsg@{
            """[${request.method}] ${request.endpoint} 
[body] ${request.entity.content.readContentString()} 
[result] ${response}
[耗时] ${db.executeTime}"""
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
        if (op(esName)) {
            logger.Important(getMsg())
        }
    }
}