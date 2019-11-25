package nbcp.db.sql

/**
 * Created by yuxh on 2018/7/3
 */

data class SqlParameterData(
        var type: Class<*>,
        var value: Any?
)

data class SqlExecuteData(
        var executeSql: String = "",
        var parameters: Array<SqlParameterData> = arrayOf()
)