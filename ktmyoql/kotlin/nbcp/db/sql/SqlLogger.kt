package nbcp.db.sql.logger

import nbcp.comm.*
import nbcp.db.db
import nbcp.db.sql.*
import nbcp.utils.SpringUtil
import org.slf4j.Logger
import org.slf4j.LoggerFactory


val sqlLog by lazy {
    return@lazy SpringUtil.getBean<SqlTableLogProperties>()
}


inline fun Logger.logQuery(
    error: Exception?,
    tableDbName: String,
    executeParameterData: SqlParameterData,
    result: Any
) {
    var getMsg: () -> String = getMsg@{
        var msg_log = mutableListOf(
            "[select] ${executeParameterData.expression}",
            "[参数] ${executeParameterData.values.ToJson()}"
        )

        if (config.debug) {
            msg_log.add("[result] ${result.ToJson()}")
        } else {
            if (result is List<*>) {
                msg_log.add("[result.size] ${result.size}")
            } else if (result is Array<*>) {
                msg_log.add("[result.size] ${result.size}")
            } else if (result is Number) {
                msg_log.add("[result] ${result}")
            } else if (result is String) {
                msg_log.add("[result] ${result}")
            }
        }

        msg_log.add("[耗时] ${db.executeTime}")
        return@getMsg msg_log.joinToString(const.line_break)
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
    if (sqlLog.getQueryLog(tableDbName)) {
        this.Important(getMsg())
    }
}


inline fun Logger.logExec(error: Exception?, tableDbName: String, executeParameterData: SqlParameterData, n: Int) {
    var getMsg: () -> String = getMsg@{
        var msg_log = mutableListOf(
            "[sql] ${executeParameterData.expression}",
            "[参数] ${executeParameterData.values.ToJson()}",
            "[result] ${n}",
            "[耗时] ${db.executeTime}"
        )

        return@getMsg msg_log.joinToString(const.line_break)
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
    if (sqlLog.getInsertLog(tableDbName) ||
        sqlLog.getUpdateLog(tableDbName) ||
        sqlLog.getDeleteLog(tableDbName)
    ) {
        this.Important(getMsg())
    }
}


inline fun Logger.logDelete(error: Exception?, tableDbName: String, executeParameterData: SqlParameterData, n: Int) {
    var getMsg: () -> String = getMsg@{
        var msg_log = mutableListOf(
            "" +
                    "[delete] ${executeParameterData.expression}",
            "[参数] ${executeParameterData.values.ToJson()}",
            "[result] ${n}",
            "[耗时] ${db.executeTime}"
        )

        return@getMsg msg_log.joinToString(const.line_break)
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
    if (sqlLog.getDeleteLog(tableDbName)
    ) {
        this.Important(getMsg())
    }
}


inline fun Logger.logInsert(error: Exception?, tableDbName: String, getMsg: () -> String) {
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
    if (sqlLog.getInsertLog(tableDbName)
    ) {
        this.Important(getMsg())
    }
}

//    fun logInsert(error: Exception?, tableDbName: String, msg: String ) {
//
//        if (error != null) {
//            logger.error(msg)
//            logger.error(error.message, error);
//            return;
//        }
//
//        if (logger.scopeInfoLevel) {
//            logger.info(msg)
//            return;
//        }
//
//
//        //如果指定了输出Sql
//        if (sqlLog.getInsertLog(tableDbName)
//        ) {
//            usingScope(LogLevel.INFO) {
//                logger.info(msg)
//            }
//        }
//    }


inline fun Logger.logUpdate(error: Exception?, tableName: String, executeParameterData: SqlParameterData, n: Int) {

    var getMsg: () -> String = getMsg@{
        var msg_log = mutableListOf(
            "[update] ${executeParameterData.expression}",
            "[参数] ${executeParameterData.values.ToJson()}",
            "[result] ${n}",
            "[耗时] ${db.executeTime}"
        )

        return@getMsg msg_log.joinToString(const.line_break)
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
    if (sqlLog.getUpdateLog(tableName)
    ) {
        this.Important(getMsg())
    }

}
