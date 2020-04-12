package nbcp.utils

import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

/**
 * Twitter的分布式自增ID雪花算法snowflake
 * 需要配置：
 * app.dataCenter-id , 默认为1，范围：1 - 31
 * app.machine-id ， 默认为1 ， 范围：1 - 31
 */
@Component
class SnowFlake : InitializingBean {
    /**
     * 数据中心Id, 默认为1，范围：1 - 31
     */
    @Value("\${app.datacenter-id:1}")
    var dataCenterId: Int = 1

    /**
     * 机器标识Id, 默认为1 ， 范围：1 - 31
     */
    @Value("\${app.machine-id:1}")
    var machineId: Int = 1

    private var sequence = 0L //序列号
    private var lastStmp = -1L //上一次时间戳
    override fun afterPropertiesSet() {
        require(!(dataCenterId > MAX_DATACENTER_NUM || dataCenterId < 0)) { "dataCenterId can't be greater than MAX_DATACENTER_NUM or less than 0" }
        require(!(machineId > MAX_MACHINE_NUM || machineId < 0)) { "machineId can't be greater than MAX_MACHINE_NUM or less than 0" }
    }

    /**
     * 产生下一个ID
     */
    @Synchronized
    fun nextId(): Long {
        val currStmp = System.currentTimeMillis()
        if (currStmp == lastStmp) { //相同毫秒内，序列号自增
            sequence = sequence + 1 and MAX_SEQUENCE
            //同一毫秒的序列数已经达到最大
            if (sequence == 0L) {
                // System.out.println("雪花算法休息 1 毫秒!");
                try {
                    Thread.sleep(1)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
                return nextId()
            }
        } else { //不同毫秒内，序列号置为0
            sequence = 0L
        }
        lastStmp = currStmp

        /**
         * //时间戳部分
         * //数据中心部分
         * //机器标识部分
         * //序列号部分
         */
        return (currStmp - START_STMP) shl TIMESTMP_LEFT or
                (dataCenterId shl DATACENTER_LEFT).toLong() or
                (machineId shl MACHINE_LEFT).toLong() or
                sequence
    }

    companion object {
        /**
         * 起始的时间戳
         */
        private const val START_STMP = 1514764800000L
        /**
         * 每一部分占用的位数
         */
        private const val SEQUENCE_BIT: Int = 12 //序列号占用的位数
        private const val MACHINE_BIT: Int = 5 //机器标识占用的位数
        private const val DATACENTER_BIT: Int = 5 //数据中心占用的位数
        /**
         * 每一部分的最大值
         */
        private const val MAX_DATACENTER_NUM = -1L xor (-1L shl DATACENTER_BIT.toInt())
        private const val MAX_MACHINE_NUM = -1L xor (-1L shl MACHINE_BIT.toInt())
        private const val MAX_SEQUENCE = -1L xor (-1L shl SEQUENCE_BIT.toInt())
        /**
         * 每一部分向左的位移
         */
        private const val MACHINE_LEFT = SEQUENCE_BIT
        private const val DATACENTER_LEFT = SEQUENCE_BIT + MACHINE_BIT
        private const val TIMESTMP_LEFT = DATACENTER_LEFT + DATACENTER_BIT
    }
}