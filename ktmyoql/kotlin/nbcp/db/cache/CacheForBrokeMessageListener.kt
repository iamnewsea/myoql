//package nbcp.db.cache
//
//import nbcp.comm.FromJson
//import nbcp.comm.config
//import nbcp.comm.const
//import nbcp.comm.minus
//import nbcp.db.db
//import org.slf4j.LoggerFactory
//import org.springframework.data.redis.connection.Message
//import org.springframework.data.redis.connection.MessageListener
//import java.time.LocalDateTime
//
//class CacheForBrokeMessageListener( ) : MessageListener {
//    companion object {
//        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
//    }
//
//    /**
//     * 收到消息会比较慢。
//     */
//    override fun onMessage(msg: Message, pattern: ByteArray?) {
//        CacheForBrokeDataWorkService.startBrokeCacheWork();
//    }
//}