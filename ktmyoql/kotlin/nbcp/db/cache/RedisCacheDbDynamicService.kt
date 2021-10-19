//package nbcp.db.cache
//
//import nbcp.comm.*
//import nbcp.db.db
//import nbcp.db.redis.scanKeys
//import nbcp.utils.CodeUtil
//import org.slf4j.LoggerFactory
//import org.springframework.beans.factory.InitializingBean
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.data.redis.connection.Message
//import org.springframework.data.redis.connection.MessageListener
//import org.springframework.data.redis.core.StringRedisTemplate
//import java.time.LocalDateTime
//import java.util.concurrent.Executors
//import kotlin.concurrent.thread
//
///**
// * 各系统继承该类，进行消费
// */
//open class RedisCacheDbDynamicService : InitializingBean {
//    companion object {
//        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
//
//        val CHANNELNAME = "sc-borker-version"
//    }
//
//
//    override fun afterPropertiesSet() {
//
////        var container = RedisMessageListenerContainer()
////        var listener = MessageListenerAdapter(SqlBrokerListener(this), "handleMessage");
////
////        container.connectionFactory = redisTemplate.connectionFactory;
////
////        container.addMessageListener(
////            listener,
////            ChannelTopic("${CHANNELNAME}-${config.applicationName}")
////        )
//
//        startSubscribe();
//    }
//
//    fun startSubscribe() {
//        thread(start = true, isDaemon = true, name = "RedisCacheDbDynamicService") {
//            while (true) {
//                try {
//                    //收到的消息会比较滞后。
//                    redisTemplate.connectionFactory.connection.subscribe(
//                        object : MessageListener {
//                            override fun onMessage(message: Message, pattern: ByteArray?) {
//                                startBrokeCacheWork()
//                            }
//                        },
//                        "${CHANNELNAME}-${config.applicationName}".toByteArray(const.utf8)
//                    )
//                } catch (ex: Exception) {
//                    //可能断网
//                    logger.error(ex.message);
//                    Thread.sleep(1000);
//                }
//            }
//        }
//    }
//
//    /**
//     * 发布队列的通知。
//     */
//    fun publish() {
//        //由于消息订阅收到消息会滞后。先执行:
//        startBrokeCacheWork()
//
//        redisTemplate.convertAndSend(
//            "${CHANNELNAME}-${config.applicationName}", CodeUtil.getCode()
//        )
//    }
//
//    /**
//     * 缓存数据源，使用系统固定的数据库，不涉及分组及上下文切换。
//     */
//    @Autowired
//    lateinit var redisTemplate: StringRedisTemplate;
//
//
//    private var working = false;
//    var lastConsumerAt = LocalDateTime.now();
//
//    /**
//     * 驱动器，驱动从 redis 队列中取出，进行消费。
//     * @param message: 仅标识有任务，没有其它意义
//     */
//    fun startBrokeCacheWork() {
//        //TODO 可能并发问题：spop 后为空退出后，有消息进入
//        if (working) {
//            if (LocalDateTime.now().minus(lastConsumerAt).toMinutes() < 3) {
//                return;
//            }
//        }
//        working = true;
//
//        Executors.newSingleThreadExecutor().execute {
//            try {
//                while (true) {
//                    var cacheBrokeStringValue = db.rer_base.sqlCacheBroker.spop(config.applicationName);
//                    if (cacheBrokeStringValue.isNullOrEmpty()) {
//                        break;
//                    }
//
//                    var cacheBroke = cacheBrokeStringValue.FromJson<BrokeRedisCacheData>()!!
//                    BrokeRedisCacheData.brokeCacheItemNoWait(cacheBroke);
//                }
//            } catch (e: Exception) {
//                logger.warn(e.message, e);
//            } finally {
//                lastConsumerAt = LocalDateTime.now();
//                working = false;
//            }
//        }
//    }
//}
//
