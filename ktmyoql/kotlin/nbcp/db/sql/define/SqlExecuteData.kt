//package nbcp.db.sql
//
//import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
//
///**
// * Created by yuxh on 2018/7/3
// */
///**
// * 自定义参数部分
// */
//data class SqlParameterData(
//        var type: Class<*>,
//        var value: Any?
//)
//
///**
// * 自定义可执行部分。
// */
//data class SqlExecuteData @JvmOverloads constructor(
//        var executeSql: String = "",
//        //程序自己标记的命名参数。不能直接使用它来执行。
//        var parameterDefines: MutableMap<String, SqlParameterData> = mutableMapOf()
//) {
//    //NamedParameterJdbcTemplate 认可的可执行匹配的参数。
//    val executeParameters: MapSqlParameterSource = MapSqlParameterSource(parameterDefines.mapValues { it.value.value })
//}