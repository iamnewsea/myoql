package nbcp.base.component

import nbcp.base.comm.config
import nbcp.base.extend.AsInt
import nbcp.base.extend.ToLocalDateTime
import nbcp.base.utils.MyUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Component
import java.time.LocalDateTime

/**
 * 源自Twitter的分布式自增ID雪花算法snowflake 的改进版
 * 需要配置：
 * app.machine-id ， 默认为1 ， 范围：1 - 1023
 *
 * 时间戳部分    43位 ， 278.92年， 2000年到 2278年
 * 机器标识部分  10位 , 1-1023 ， 共 1023个机器
 * 序列号部分    10位 , 0-1023 , 每毫秒 1024 个值
 */
@Component
class SnowFlake : InitializingBean {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)

        /**
         * 起始的时间戳 = 2000年1月1日的毫秒數
         */
        private const val START_STMP = 946684800000L

        /**
         * 每一部分占用的位数
         */
        private const val SEQUENCE_BIT: Int = 10 //序列号占用的位数
        private const val MACHINE_BIT: Int = 10  //机器标识占用的位数

        /**
         * 每一部分的最大值
         */
//        private const val MAX_MACHINE_NUM = -1L xor (-1L shl MACHINE_BIT)
        private const val MAX_SEQUENCE = -1L xor (-1L shl SEQUENCE_BIT)

        /**
         * 每一部分向左的位移
         */
        private const val MACHINE_LEFT = SEQUENCE_BIT
        private const val TIMESTMP_LEFT = SEQUENCE_BIT + MACHINE_BIT

        fun getLocalDateTimeValue(code: String): LocalDateTime {
            var value = java.lang.Long.valueOf(code, 36);
            return ((value shr TIMESTMP_LEFT) + START_STMP).ToLocalDateTime()
        }
    }

    /**
     * 机器标识Id, 默认为1 ， 范围：1 - 1023
     */
    var machineId: Int = MyUtil.getRandomNumber(100, 1000);

    private var sequence = 0L //序列号
    private var lastStmp = -1L //上一次时间戳
    override fun afterPropertiesSet() {
        val appMachineId = config.getConfig("app.machine-id").AsInt()

        if (appMachineId >= 1) {
            this.machineId = ((appMachineId % 128) shl 3) + MyUtil.getRandomNumber(0, 8).AsInt();
        }
    }

    /**
     * 产生下一个ID
     */
    @Synchronized
    fun nextId(): Long {
        val currStmp = System.currentTimeMillis()
        if (currStmp == lastStmp) { //相同毫秒内，序列号自增
            sequence = (sequence + 1) and MAX_SEQUENCE
            //同一毫秒的序列数已经达到最大
            if (sequence == 0L) {
                logger.info("雪花算法休息 1 毫秒!");
                try {
                    Thread.sleep(1)
                } catch (e: Exception) {
                    logger.error("线程sleep出错", e)
                }
                return nextId()
            }
        } else { //不同毫秒内，序列号置为0
            sequence = 0L
        }
        lastStmp = currStmp

        return (currStmp - START_STMP) shl TIMESTMP_LEFT or
                (machineId shl MACHINE_LEFT).toLong() or
                sequence
    }
}