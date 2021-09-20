@file:JvmName("MyHelper")
@file:JvmMultifileClass

package nbcp.comm

import java.io.InputStream


/**
 * 从流中读取出内容，注意，可能只能读取一次。
 */
fun InputStream.ReadContentStringFromStream(): String {
    return String(this.readBytes(), const.utf8)
}