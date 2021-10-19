package nbcp.db.redis

import nbcp.comm.HasValue
import nbcp.comm.config
import org.springframework.data.redis.serializer.StringRedisSerializer

/**
 * 带产品线前缀的 Redis Key
 */
class MyRedisKeySerializerWithProductLine : StringRedisSerializer() {
    override fun serialize(key: String): ByteArray {
        var key2 = key;
        if (config.productLineCode.HasValue &&
            !key2.startsWith(config.productLineCode + ":")
        ) {
            key2 = config.productLineCode + ":" + key2;

        }
        return super.serialize(key2)
    }

    override fun deserialize(bytes: ByteArray?): String {
        var key2 = super.deserialize(bytes)
        if (config.productLineCode.HasValue &&
            !key2.startsWith(config.productLineCode + ":")
        ) {
            key2 = key2.substring(config.productLineCode.length + 1);

        }
        return key2;
    }
}