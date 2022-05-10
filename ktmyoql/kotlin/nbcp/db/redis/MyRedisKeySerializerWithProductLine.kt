package nbcp.db.redis

import nbcp.comm.HasValue
import nbcp.comm.config
import nbcp.comm.scopes
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

        if (prefix.HasValue && !key2.startsWith(prefix + ":")) {
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
            return config.productLineCode;
        }

        return "";
    }
}