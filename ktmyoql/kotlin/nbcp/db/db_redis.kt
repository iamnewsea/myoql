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