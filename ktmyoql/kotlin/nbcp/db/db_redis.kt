package nbcp.db


import nbcp.comm.*
import nbcp.db.redis.RedisDataSource
import nbcp.db.redis.RedisTemplateScope
import nbcp.utils.*
import org.springframework.data.redis.core.StringRedisTemplate

/**
 * 请使用 db.mongo
 */
object db_redis {

    fun getStringRedisTemplate(group: String): StringRedisTemplate {
        val config = SpringUtil.getBean<RedisDataSource>();
        val dataSourceName = config.getDataSourceName(group)
        if (dataSourceName.HasValue) {
            return SpringUtil.getBean(dataSourceName) as StringRedisTemplate
        }

        return scopes.getLatest<RedisTemplateScope>()?.value
            ?: SpringUtil.getBean<StringRedisTemplate>()
    }
}