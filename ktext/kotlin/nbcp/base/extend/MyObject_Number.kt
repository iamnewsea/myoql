package nbcp.base.extend

import java.text.DecimalFormat


/*分 转化为 可读的金额。单位： 亿，万，元。
* */
fun Long.ToReadableAmountValue(): String {
    var value = this;
    var ret = StringBuilder();
    var yi_fen = 10000000000L
    if (value < 0) {
        ret.append("-");
        value = Math.abs(value);
    }

    if (value > yi_fen) {
        ret.append(value / yi_fen);
        ret.append("亿");
        value = value % yi_fen;
    }

    var wan_fen = 1000000;
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
    return ret.toString() + "元"
}

/**
 * 转换为每个幂数。 如： 7 转换为 ，0，1，2
 */
fun Long.ToBitPowerValue(): List<Int> {
    var ret = mutableListOf<Int>()
    var value = this;
    var i = 0 ;
    while (true) {
        if (value == 0L) {
            break;
        }
        if (value and 1 == 1L) {
            ret.add(  i )
        }

        i++;

        value = value ushr 1;
    }
    return ret;
}

