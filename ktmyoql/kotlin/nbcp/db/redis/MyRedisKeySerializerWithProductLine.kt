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
        if (config.productLineName.HasValue &&
            !key2.startsWith(config.productLineName + ":")
        ) {
            key2 = config.productLineName + ":" + key2;

        }
        return super.serialize(key2)
    }

    override fun deserialize(bytes: ByteArray?): String {
        var key2 = super.deserialize(bytes)
        if (config.productLineName.HasValue &&
            !key2.startsWith(config.productLineName + ":")
        ) {
            key2 = key2.substring(config.productLineName.length + 1);

        }
        return key2;
    }
}