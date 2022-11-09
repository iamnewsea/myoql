@file:JvmName("MyHelper")
@file:JvmMultifileClass

package nbcp.base.extend

import java.text.DecimalFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.reflect.KClass


/*分 转化为 可读的金额。单位： 亿，万，元。
* 1234123456789L => 123亿4123万4567.89元
* */
fun Long.ToReadableAmountValue(): String {
    var value = this;
    var ret = StringBuilder();
    var yi_fen = 1_0000_0000_00L
    if (value < 0) {
        ret.append("-");
        value = Math.abs(value);
    }

    if (value > yi_fen) {
        ret.append(value / yi_fen);
        ret.append("亿");
        value = value % yi_fen;
    }

    var wan_fen = 1_0000_00;
    if (value > wan_fen) {
        ret.append(value / wan_fen);
        ret.append("万");
        value = value % wan_fen;
    }

    if (value > 0) {
        if (value.toInt() % 100 == 0) {
            ret.append(value / 100);
        } else {
            ret.append(DecimalFormat("#.##").format(value / 100.0));
        }
    }

    ret.append("元")
    return ret.toString()
}

/**
 * 转换为每个幂数。 如： 7 转换为 ，0，1，2
 */
fun Long.ToBitPowerValue(): List<Int> {
    var ret = mutableListOf<Int>()
    var value = this;
    var i = 0;
    while (true) {
        if (value == 0L) {
            break;
        }
        if (value and 1 == 1L) {
            ret.add(i)
        }

        i++;

        value = value ushr 1;
    }
    return ret;
}

/**
 * 毫秒数转为 LocalDateTime
 */
fun Long.ToLocalDateTime(): LocalDateTime {
    return LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneId.systemDefault())
}

inline fun <reified T> Int.ToEnum(): T? {
    return this.ToEnum(T::class.java)
}

fun <T : Any> Int.ToEnum(enumClazz: KClass<T>): T? {
    return this.ToEnum(enumClazz.java)
}

//通过 int value 找.
fun <T> Int.ToEnum(enumClazz: Class<T>): T? {
    if (enumClazz.isEnum == false) return null;
    var numberField = enumClazz.GetEnumNumberField();
    if (numberField == null) return null;

    numberField.isAccessible = true;
    return enumClazz.GetEnumList().firstOrNull { numberField.get(it).AsInt() == this }
}

/**
 * 2位小数： #0.00
 * 百分数(两位小数)： ##.00%
 * 百分数（不带小数）： ##%
 */
@JvmOverloads
fun Number.Format(format: String = ""): String {
    if (format.isEmpty()) {
        return this.toString();
    }
    //https://www.cnblogs.com/Small-sunshine/p/11648652.html
    return DecimalFormat(format).format(this)
}