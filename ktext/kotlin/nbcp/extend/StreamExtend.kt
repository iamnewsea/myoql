@file:JvmName("MyHelper")
@file:JvmMultifileClass

package nbcp.comm

import java.io.InputStream


fun InputStream.GetHtmlString(): String {
    return String(this.readBytes(), const.utf8)
}