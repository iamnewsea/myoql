package nbcp.base.utils

import nbcp.base.comm.JsonMap
import nbcp.base.comm.StringKeyMap
import nbcp.base.comm.StringMap
import nbcp.base.comm.const
import nbcp.base.extend.*
import org.slf4j.LoggerFactory
import org.springframework.core.io.ClassPathResource
import java.io.File
import java.lang.reflect.Field
import java.nio.charset.StandardCharsets
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.reflect.KClass


/**
 * Created by nbcp on 2017/3/28.
 */

/**
 * 工具类。
 */

object MyUtil {
    internal val logger = LoggerFactory.getLogger(this::class.java)

    //    val OneHourSeconds: Int = 3600000;
//    val OneDayMilliseconds: Int = 86400000;
    @JvmStatic
    val OneDaySeconds: Int = 86400;

    @JvmStatic
    val SystemTimeZoneOffsetTotalSeconds =
        ZoneId.systemDefault().rules.getStandardOffset(Date().toInstant()).totalSeconds  //系统时区相差的秒数

    @JvmStatic
    private val random = Random(System.nanoTime());

    //    /**
//     * 北京时间的今天凌晨。
//     */
    @JvmStatic
    val today: LocalDate
        get() {
            return LocalDate.now();
        }

    @JvmStatic
    val availableCpuProcessors: Int by lazy {
        return@lazy Runtime.getRuntime().availableProcessors()
    }






    /**
     * 获取正在执行的方法信息
     */
    @JvmStatic
    fun getCurrentMethodInfo(): StackTraceElement {
        return Thread.currentThread().getStackTrace()[2]!!
    }



//    /**
//     * 按大写字母拆分
//     */
//    fun splitWith(value: String,splitCallback:((Char)->Boolean)): List<String> {
//        var ret = mutableListOf<String>()
//
//        var prevIndex = 0;
//        for (index in 1 until value.length) {
//            var item = value[index];
//
//            if (item.isUpperCase()) {
//                ret.add(value.substring(prevIndex, index))
//                prevIndex = index;
//            }
//        }
//
//        ret.add(value.substring(prevIndex));
//
//        return ret.filter { it.HasValue };
//    }




    /**
     * 生成大于等于0，小于指定最大值的随机数,即 [0,max)
     */
    @JvmStatic
    fun getRandomNumber(min: Int, max: Int): Int {
        var start = Math.min(min, max);
        var end = Math.max(min, max);
        var base = end - start;
        return (Math.abs(random.nextInt()) % base) + start
    }

    /**
     * 生成指定长度的随机数
     */
    @JvmStatic
    fun getRandomWithLength(length: Int, vararg withoutChars: Char): String {
        var ret = "";
        while (true) {
            ret += Math.abs(random.nextInt()).toString(36).remove(*withoutChars);
            if (ret.length >= length) {
                break;
            }
        }
        ret = ret.slice(0 until length);
        return ret;
    }




//
//    /**
//     * @param days: 从1开始的天数。（mongo dayOfYear 返回的天数）
//     */
//    fun getDate(year: Int, days: Int): Date {
//        val format = java.text.SimpleDateFormat("yyyy-MM-dd")
//
//        var currentYear0 = format.parse("${year}-01-01")
//        return Date(currentYear0.time + (days - 1) * OneDayMilliseconds);
//    }
//
//    //判断是否是北京时间的同一天。
//    fun isSameBjDay(date1: Date, date2: Date): Boolean {
//        return (date1.time + 8 * OneHourMilliseconds) / OneDayMilliseconds == (date2.time + 8 * OneHourMilliseconds) / OneDayMilliseconds;
//    }

//----------------------

    @JvmStatic
    inline fun <reified T> checkEmpty(obj: T, mapDefine: HashMap<String, String>): String {
        var type = T::class.java;
        for (key in mapDefine.keys) {
            if (key.contains('.') == false) {
                var m = type.getMethod("get" + key[0].uppercaseChar() + key.substring(1));
                if (m == null) {
                    continue;
                }

                var v = m.invoke(obj);
                if (v == null || v.toString().isEmpty()) {
                    return mapDefine[key]!!
                }
                continue;
            }
        }
        return "";
    }


//    fun setPropertyValue(entity: Any, property: String, value: Any?) {
//        var type = entity::class.java.AllFields.firstOrNull { it.name == property };
//        if (type == null) {
//            return;
//        }
////        type.isAccessible = true;
//        type.set(entity, value)
//    }

    /**
     * 通过反射把 源对象值传输到目标对象
     */
//    @JvmStatic
//    fun transportValueWithReflect(src: Any, target: Any) {
//        var srcFields = src::class.java.AllFields;
//        target::class.java.AllFields.forEach { targetField ->
//            var srcField = srcFields.firstOrNull { it.name == targetField.name }
//            if (srcField == null) {
//                return@forEach
//            }
//            targetField.set(target, srcField.get(src))
//        }
//    }


    /**
     * 用公钥加密
     *
     * @param targetId
     * @param publicSecret
     * @return
     */
    @JvmStatic
    fun encryptWithPublicSecret(targetId: String, publicSecret: String?): String? {
        val dt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
        val text = """
             $targetId
             $dt
             ${MyUtil.getRandomWithLength(6)}
             """.trimIndent()
        val secretByte = Base64.getDecoder().decode(publicSecret)
        val encrypt =
            RSARawUtil.encryptByPublicKey(text.toByteArray(StandardCharsets.UTF_8), secretByte)

        // + / = 替换为： - * ~
        return Base64.getEncoder()
            .encodeToString(encrypt)
            .replace("+", "-")
            .replace("/", "*")
            .replace("=", "~") + "." + dt
    }


    /**
     * 用私钥解密
     *
     * @param encryptWithPublicSecretValue
     * @param privateSecret
     * @return
     */
    @JvmStatic
    fun decryptWithPrivateSecret(encryptWithPublicSecretValue: String, privateSecret: String?): String? {
        val dotIndex = encryptWithPublicSecretValue.indexOf(".")
        if (dotIndex <= 0) throw RuntimeException("非法值")
        val oriValue = Base64.getDecoder().decode(
            encryptWithPublicSecretValue.substring(0, dotIndex)
                .replace("-", "+")
                .replace("*", "/")
                .replace("~", "=")
        )
        val dtStringValue = encryptWithPublicSecretValue.substring(dotIndex + 1)
        val secretByte = Base64.getDecoder().decode(privateSecret)
        val oriStrings =
            String(
                nbcp.base.utils.RSARawUtil.decryptByPrivateKey(oriValue, secretByte),
                StandardCharsets.UTF_8
            ).split("\n")
                .toTypedArray()
        if (oriStrings.size < 3) throw RuntimeException("非法值")
        if (dtStringValue != oriStrings[1]) {
            throw RuntimeException("非法值")
        }
        val dt = LocalDateTime.parse(dtStringValue, DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
        val dd = Duration.between(dt, LocalDateTime.now()).abs()
        if (dd.seconds > 60) {
            throw RuntimeException("已过期")
        }
        return oriStrings[0]
    }


    /**
     * 比较版本
     * @return 相等返回0, 大于返回1,小于返回 -1
     */
    @JvmStatic
    fun compareVersion(v1: String, v2: String): Int {
        if (v1 == v2) return 0;
        if (v1.isEmpty()) return -1;
        if (v2.isEmpty()) return 1;

        //按 . 分隔，比较每个部分。
        var v1Sects = Regex("\\d+").splitBoundary(v1)
        var v2Sects = Regex("\\d+").splitBoundary(v2)

        var commonLen = Math.min(v1Sects.size, v2Sects.size)
        for (i in 0 until commonLen) {
            var v1v = v1Sects[i];
            var v2v = v2Sects[i];

            var c_ret = 0
            if (v1v.IsNumberic() && v2v.IsNumberic()) {
                c_ret = v1v.AsInt().compareTo(v2v.AsInt())
            } else {
                //快照版本 1.0-SNAPSHOT，小于 正式版本 1.0
                //先提取数字比较
                c_ret = v1v.compareTo(v2v);
            }

            if (c_ret != 0) {
                return c_ret;
            }
        }

        //数字相同的情况下， 越短，版本越大。 如带有 -SNAPSHOT的版本要小。
        if (v1Sects.size > commonLen) return -1;
        else if (v2Sects.size > commonLen) return 1

        return 0;
    }
}

