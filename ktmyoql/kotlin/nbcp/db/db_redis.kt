package nbcp.db


import nbcp.comm.*
import nbcp.db.cache.CacheForSelect
import nbcp.db.cache.CacheForSelectData
import nbcp.db.redis.RedisDataSource
import nbcp.utils.*
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.expression.spel.standard.SpelExpressionParser
import org.springframework.expression.spel.support.StandardEvaluationContext

/**
 * 请使用 db.mongo
 */
object db_redis {

    /**
     * key规则：5部分： sc:{主表}:{join_tables.sort().join(":")}:{主表key}@{key_value}:{sql/md5}
     * 如： sc:主表:|join_tab1|join_tab2|:cityCode-010:select*from主表wherecityCode=010anddeleted!=0
     * 主表规则：  sc:表:*
     * join表规则:  sc:*|表|* , join表为空，没有|
     * 主表key:   sc:*:表key@*
     * 表主value:  sc:*@表value:*
     *
     * 约束:  每个部分不能出现半角 冒号，竖线，@,出现部分用全角代替
     *
     * scan:   sc:table1:*:table1_column1-value1:*
     */
    fun getCacheKey(cache: CacheForSelectData): String {

        var ret = mutableListOf<String>();
        ret.add("sc")
        ret.add(cache.table);

        if (cache.joinTables.any()) {
            ret.add("|" + cache.joinTables.toSortedSet().joinToString("|") + "|")
        } else {
            ret.add("")
        }


        if (cache.key.HasValue && cache.value.HasValue) {
            ret.add("${cache.key}-${cache.value}")
        }

        var ext = cache.sql

        if (ext.length > 32) {
            ret.add(Md5Util.getBase64Md5(ext))
        } else {
            ret.add(ext)
        }
        return ret.joinToString(":")
    }


    fun getStringRedisTemplate(group: String): StringRedisTemplate {
        var config = SpringUtil.getBean<RedisDataSource>();
        var dataSourceName = config.getDataSourceName(group)
        if (dataSourceName.HasValue) {
            return SpringUtil.getBean(dataSourceName) as StringRedisTemplate
        }

        return scopes.GetLatest<StringRedisTemplate>()
            ?: SpringUtil.getBean<StringRedisTemplate>()
    }
}