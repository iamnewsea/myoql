@file:JvmName("MyHelper")
@file:JvmMultifileClass

package nbcp.base.extend

import nbcp.base.comm.const
import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.nio.ByteBuffer


/**
 * 从流中读取出内容，注意，可能只能读取一次。
 */
fun InputStream.readContentString(): String {
    return String(this.readBytes(), const.utf8)
}

/**
 * ByteBuffer 转 ByteArray
 */
fun ByteBuffer.toByteArray(): ByteArray {
    val copy = ByteArray(this.remaining())
    this.get(copy)
    return copy
}

fun ByteArray.toUtf8String(): String {
    return this.toString(const.utf8)
}

/**
 * 把流转化为可重复读的流。
 * @return 可能返回 BufferedInputStream! (效果比ByteArrayInputStream好)
 */
fun InputStream.prepareToMarkSupportedInputStream(): InputStream {
    if (this.markSupported()) return this;
    return BufferedInputStream(this);
}

/**
 * 把流全部读入内存，实现可重复读，内存压力会大
 */
fun InputStream.readToMemoryStream(): ByteArrayInputStream {
    this.use {
        return ByteArrayInputStream(this.readBytes());
    }
}
