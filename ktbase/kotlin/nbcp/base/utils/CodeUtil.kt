package nbcp.base.utils

import nbcp.base.component.SnowFlake
import java.time.LocalDateTime

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
    @JvmStatic
    fun getNumberValue(): Long {
        return snowFlake.nextId();
    }

    /**
     * 雪花算法 生成36进制字符串形式,一般是12位长度。
     */
    @JvmStatic
    fun getCode(): String {
        //使用 java.lang.Long.valueOf(36进制数,36) 转化为Long
        return snowFlake.nextId().toString(36);
    }

    @JvmStatic
    fun getDateTimeFromCode(code: String): LocalDateTime {
        return SnowFlake.getLocalDateTimeValue(code);
    }
}