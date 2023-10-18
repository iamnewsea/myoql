package nbcp.myoql.db


import nbcp.base.extend.HasValue
import nbcp.base.extend.scopes
import nbcp.base.utils.SpringUtil
import nbcp.myoql.db.redis.RedisDataSource
import nbcp.myoql.db.redis.RedisRenewalDynamicService
import nbcp.myoql.db.redis.RedisTemplateScope
import nbcp.myoql.db.redis.scanKeys
import org.springframework.data.redis.core.StringRedisTemplate

/**
 * 请使用 db.mongo
 */
object DbRedis {


    /***
     * 删除键，使键过期。
     * 如果参数为空，则删除group键
     */
    @JvmStatic
    fun deleteKeys(vararg keys: String): Long {
        if (keys.any() == false) {
            return 0;
        }
        RedisRenewalDynamicService.clearDelayRenewalKeys(*keys);

        var group = "";
        var groups = keys.map { it.split(":").first() };
        if (groups.size == 1) {
            group = groups.first();
        }
        return nbcp.myoql.db.DbRedis.getStringRedisTemplate(group).delete(keys.map { it });
    }

    @JvmOverloads
    @JvmStatic
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

    @JvmStatic
    fun scanKeys(pattern: String): List<String> {
        return nbcp.myoql.db.DbRedis.scanKeys("", pattern);
    }

    /**
     * 扫描Redis key
     */
    @JvmStatic
    fun scanKeys(group: String, pattern: String): List<String> {
        var list = mutableListOf<String>()
         getStringRedisTemplate(group).scanKeys(pattern, callback = {
            list.add(it);
            true;
        });
        return list;
    }

}