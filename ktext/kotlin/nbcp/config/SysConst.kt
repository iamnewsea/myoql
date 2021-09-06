@file:JvmMultifileClass

package nbcp.comm

import nbcp.utils.MyUtil
import nbcp.utils.SpringUtil
import org.slf4j.Logger

import java.nio.charset.Charset

object const {
    /**
     * Created by yuxh on 2018/11/13
     */
    val utf8: Charset = Charset.forName("utf-8")

    //换行符符
    val line_break: String = System.getProperty("line.separator")

    /**
     * 1MB
     */
    val size1m: Int = 1048576;
}


/**
 * 获取参数 debug，如果是调试模式，那么查询日志显示结果集，会显示插入数据
 */
val Logger.debug get() = config.debug;