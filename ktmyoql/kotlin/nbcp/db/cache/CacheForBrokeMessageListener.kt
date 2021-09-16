package nbcp.db.cache

import nbcp.comm.FromJson
import nbcp.comm.config
import nbcp.comm.const
import nbcp.db.db
import org.slf4j.LoggerFactory
import org.springframework.data.redis.connection.Message
import org.springframework.data.redis.connection.MessageListener
import java.time.LocalDateTime

class CacheForBrokeMessageListener(var consumer:(BrokeRedisCacheData)->Unit) : MessageListener {
    companion object{
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
        private var working = false;

        var lastConsumerAt = LocalDateTime.now();
    }
    /**
     * 驱动器，驱动从 redis 队列中取出，进行消费。
     */
    fun handleMessage(message: String) {
        if (working) return;
        working = true;

        while (true) {
            var cacheBrokeStringValue = db.rer_base.sqlCacheBroker.spop(config.applicationName);
            if (cacheBrokeStringValue.isNullOrEmpty()) {
                break;
            }

            var cacheBroke = cacheBrokeStringValue.FromJson<BrokeRedisCacheData>()!!

            consumer(cacheBroke);
        }

        lastConsumerAt = LocalDateTime.now();
        working = false;
        logger.info("消费完成! ${lastConsumerAt}")
    }

    override fun onMessage(msg: Message, pattern: ByteArray?) {
        handleMessage(String(msg.body, const.utf8));
    }
}