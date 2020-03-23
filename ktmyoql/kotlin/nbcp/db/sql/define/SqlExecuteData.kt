package nbcp.db.sql

/**
 * Created by yuxh on 2018/7/3
 */
/**
 * 自定义参数部分
 */
data class SqlParameterData(
        var type: Class<*>,
        var value: Any?
)

/**
 * 自定义可执行部分。
 */
data class SqlExecuteData(
        var executeSql: String = "",
        //程序自己标记的命名参数。不能直接使用它来执行。
        var parameters: Array<SqlParameterData> = arrayOf()
) {
    //jdbcTemplate认可的可执行匹配的参数。
    val executeParameters: Array<Any?> = parameters.map { it.value }.toTypedArray()
}