@file:JvmName("MyHelper")
@file:JvmMultifileClass

package nbcp.comm

import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream


/**
 * 从流中读取出内容，注意，可能只能读取一次。
 */
fun InputStream.readContentString(): String {
    return String(this.readBytes(), const.utf8)
}


/**
 * 把流转化为可重复读的流。
 * @return 可能返回 BufferedInputStream! (效果比ByteArrayInputStream好)
 */
fun InputStream.prepareToMarkSupportedInputStream() :InputStream{
    if( this.markSupported()) return this;
    return BufferedInputStream(this);
}