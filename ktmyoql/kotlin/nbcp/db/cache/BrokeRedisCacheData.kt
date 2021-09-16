package nbcp.db.cache

import nbcp.comm.*
import nbcp.db.db
import nbcp.utils.SpringUtil


data class BrokeRedisCacheData(
    var table: String,
    /**
     * 破坏表的隔离键，如: "cityCode"
     */
    var key: String,
    /**
     * 破坏表的隔离键值，如: "010"
     */
    var value: String
) {
    constructor() : this("", "", "") {
    }

    fun brokeCache() {
        db.rer_base.sqlCacheBroker.add(config.applicationName, this.ToJson());

        //发布消息通知
        SpringUtil.getBean<RedisCacheDbDynamicService>().publish()
    }

    fun getTablePattern(): String {
        return "${FromRedisCacheData.SQL_CACHE_PREFIX}${FromRedisCacheData.GROUP_JOIN_CHAR}${this.table}${FromRedisCacheData.GROUP_JOIN_CHAR}*";
    }

    fun getJoinTablePattern(): String {
        return "${FromRedisCacheData.SQL_CACHE_PREFIX}${FromRedisCacheData.GROUP_JOIN_CHAR}*${FromRedisCacheData.JOIN_TABLE_CHAR}${this.table}${FromRedisCacheData.JOIN_TABLE_CHAR}*"
    }

    companion object {
        fun of(cacheForBroke: BrokeRedisCache, variableMap: JsonMap): BrokeRedisCacheData {
            var spelExecutor = CacheKeySpelExecutor(variableMap);
            var ret = BrokeRedisCacheData();
            ret.table = spelExecutor.getVariableValue(cacheForBroke.table);
            ret.key = spelExecutor.getVariableValue(cacheForBroke.key);
            ret.value = spelExecutor.getVariableValue(cacheForBroke.value);
            return ret;
        }
    }
}
