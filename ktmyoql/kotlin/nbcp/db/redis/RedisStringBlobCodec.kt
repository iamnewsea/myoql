//package nbcp.db.redis
//
//import io.lettuce.core.codec.ByteArrayCodec
//import io.lettuce.core.codec.RedisCodec
//import io.lettuce.core.codec.StringCodec
//import java.nio.ByteBuffer
//
//
//class RedisStringBlobCodec private constructor() : RedisCodec<String, ByteArray> {
//
//    companion object {
//        val INSTANCE = RedisStringBlobCodec()
//    }
//
//    override fun encodeKey(key: String): ByteBuffer {
//        return StringCodec.UTF8.encodeKey(key)
//    }
//
//    override fun encodeValue(value: ByteArray): ByteBuffer {
//        return ByteArrayCodec.INSTANCE.encodeValue(value)
//    }
//
//    override fun decodeKey(bytes: ByteBuffer): String {
//        return StringCodec.UTF8.decodeKey(bytes)
//    }
//
//    override fun decodeValue(bytes: ByteBuffer): ByteArray {
//        return ByteArrayCodec.INSTANCE.decodeValue(bytes)
//    }
//}
