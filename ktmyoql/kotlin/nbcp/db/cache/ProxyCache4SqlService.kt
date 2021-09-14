//package nbcp.db.cache
//
//
//import nbcp.db.sql.SingleSqlData
//
//
//interface ProxyCache4SqlService   {
//    fun isEnable(): Boolean;
//    fun clear(tableName:String="")
//
//    fun getCacheKey(sql: SingleSqlData): CacheForSelectData
//
//    fun getCacheJson(cacheKey: CacheForSelectData): String
//    fun setCacheJson(cacheKey: CacheForSelectData, cacheJson: String)
//
//    fun updated4BrokeCache(sql: SingleSqlData)
//    fun delete4BrokeCache(sql: SingleSqlData)
//
//    fun insert4BrokeCache(sql: SingleSqlData)
//    fun insertMany4BrokeCache(tableName: String)
//    fun insertSelect4BrokeCache(tableName: String)
//}