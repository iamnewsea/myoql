package nbcp.myoql.db.redis

import nbcp.base.comm.*;
import nbcp.base.db.*;
import nbcp.base.enums.*;
import nbcp.base.extend.*;
import nbcp.base.utils.*;
import nbcp.myoql.db.enums.*
import nbcp.myoql.db.*;
import nbcp.myoql.db.comm.*
import org.springframework.data.redis.serializer.StringRedisSerializer

/**
 * 带产品线前缀的 Redis Key
 */
class MyRedisKeySerializerWithProductLine : StringRedisSerializer() {
    override fun serialize(key: String): ByteArray {
        var key2 = key;
        val prefix = getProductLineCodePrefix();

        if (prefix.HasValue && !key2.startsWith(prefix + ":")) {
            key2 = prefix + ":" + key2;
        }
        return super.serialize(key2)
    }

    override fun deserialize(bytes: ByteArray?): String {
        var key2 = super.deserialize(bytes)
        val prefix = getProductLineCodePrefix();

        if (prefix.HasValue && key2.startsWith(prefix + ":")) {
            key2 = key2.substring(prefix.length + 1);
        }
        return key2;
    }

    private fun getProductLineCodePrefix(): String {
        var productLineCodeScope = scopes.getLatest(RedisProductLineScope::class.java)
        if (productLineCodeScope != null) {
            return productLineCodeScope.productLineCode;
        }

        if (config.redisProductLineCodePrefixEnable) {
            return config.appPrefix;
        }

        return "";
    }
}