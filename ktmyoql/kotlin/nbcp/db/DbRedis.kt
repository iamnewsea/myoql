package nbcp.db


import nbcp.comm.*
import nbcp.db.cache.*
import nbcp.db.redis.RedisDataSource
import nbcp.db.redis.RedisTemplateScope
import nbcp.db.redis.scanKeys
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

    fun getStringRedisTemplate(group: String = ""): StringRedisTemplate {
        if (group.HasValue) {
            val config = SpringUtil.getBean<RedisDataSource>();
            val dataSourceName = config.getDataSourceName(group)
            if (dataSourceName.HasValue) {
                return SpringUtil.getBean(dataSourceName) as StringRedisTemplate
            }
        }

        return scopes.getLatest<RedisTemplateScope>()?.value
            ?: SpringUtil.getBean<StringRedisTemplate>()
    }

    fun scanKeys(pattern: String): List<String> {
        return scanKeys("", pattern);
    }

    /**
     * 扫描Redis key
     */
    fun scanKeys(group: String, pattern: String): List<String> {
        var list = mutableListOf<String>()
        getStringRedisTemplate(group).scanKeys(pattern, callback = {
            list.add(it);
            return@scanKeys true;
        });
        return list;
    }

}