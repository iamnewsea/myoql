package nbcp.db.cache

import nbcp.db.CacheKey
import nbcp.db.sql.SingleSqlData


interface ProxyCache4SqlService   {
    fun isEnable(): Boolean;
    fun clear(tableName:String="")

    fun getCacheKey(sql: SingleSqlData): CacheKey

    fun getCacheJson(cacheKey: CacheKey): String
    fun setCacheJson(cacheKey: CacheKey, cacheJson: String)

    fun updated4BrokeCache(sql: SingleSqlData)
    fun delete4BrokeCache(sql: SingleSqlData)

    fun insert4BrokeCache(sql: SingleSqlData)
    fun insertMany4BrokeCache(tableName: String)
    fun insertSelect4BrokeCache(tableName: String)
}