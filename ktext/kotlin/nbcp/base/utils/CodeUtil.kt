package nbcp.utils

import org.slf4j.LoggerFactory
import nbcp.comm.Lock
import java.math.BigInteger
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField
import java.util.*

/**
 * Created by udi on 17-5-22.
 */

object CodeUtil {

    //系统启动时，需要设置参数。
    val snowFlake: SnowFlake by lazy {
        return@lazy SpringUtil.getBean<SnowFlake>()
    }

    /**
     *  雪花算法生成Long型Id.
     */
    @Deprecated("尽量使用 getCode 方法生成String类型的编码！")
    fun getNumberValue(): Long {
        return snowFlake.nextId();
    }

    /**
     * 雪花算法生成Code
     */
    fun getCode(): String {
        return snowFlake.nextId().toString(36);
    }
}