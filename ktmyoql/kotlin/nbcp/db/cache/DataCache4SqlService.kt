//package nbcp.db
//
//import nbcp.comm.*
//import nbcp.db.sql.SingleSqlData
//
//interface DataCache4SqlService {
//    fun isEnable(): Boolean;
//    fun clear(tableName:String="")
//    fun getCacheSeconds(tableName:String?):Int
//
//
//    fun getCacheJson(cacheKey: CacheKey): String
//    fun setCacheJson(cacheKey: CacheKey, cacheJson: String)
//
//    fun brokeCache(tableName:String,keys:Set<String>)
//}
//
