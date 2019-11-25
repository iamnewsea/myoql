package nbcp.db

import nbcp.base.comm.JsonMap
import nbcp.base.comm.StringMap
import nbcp.db.sql.SingleSqlData

interface IDataCache4Sql {
    fun isEnable(): Boolean;
    fun clear(tableName:String="")
    fun getCacheSeconds(tableName:String?):Int


    fun getCacheJson(cacheKey: CacheKey): String
    fun setCacheJson(cacheKey: CacheKey, cacheJson: String)

    fun brokeCache(tableName:String,keys:Set<String>)
}

interface IProxyCache4Sql   {
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


data class UrkInfo(val rks: StringMap, val uks: StringMap, val rksValid: Boolean, val uksValid: Boolean)

