package nbcp.db


import nbcp.comm.*
import nbcp.db.cache.*
import nbcp.db.redis.RedisDataSource
import nbcp.db.redis.RedisTemplateScope
import nbcp.utils.*
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.StringRedisTemplate
import java.time.Duration
import java.util.function.Supplier
import kotlin.reflect.KClass

/**
 * 请使用 db.mongo
 */
object DbRedis {


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