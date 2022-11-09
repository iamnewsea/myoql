@file:JvmName("MyHelper")
@file:JvmMultifileClass

package nbcp.base.extend

/**
 * 使用指定字符，初始化字符串
 */
fun Char.NewString(count: Int): String {
    var chrs = StringBuilder();

    for (i in 0..count - 1) {
        chrs.append(this);
    }

    return chrs.toString();
}

/**
 * 是否是特殊字符。 即=127号范围内的 非数字非字母。
 */
val Char.IsSpecialChar: Boolean
    get() {
        if (this.code > 127) return false;
        return this.isLetterOrDigit()
    }