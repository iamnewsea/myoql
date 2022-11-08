@file:JvmName("MyHelper")
@file:JvmMultifileClass

package nbcp.base.extend

import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.ByteArrayInputStream
import java.lang.StringBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.TransformerFactory
import kotlin.reflect.KClass

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