package nbcp.web


import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStream
import java.io.PrintWriter
import java.util.*
import javax.servlet.ServletOutputStream
import javax.servlet.WriteListener
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletResponseWrapper

/**
 * Created by udi on 17-4-3.
 */

class MyHttpResponseWrapper(
        private val response: HttpServletResponse
) : HttpServletResponseWrapper(response) {
    private val out: ByteServletOutputStream
    private val writer: PrintWriter

    @Throws(IOException::class)
    override fun getOutputStream(): ServletOutputStream {
        return this.out
    }

    @Throws(IOException::class)
    override fun getWriter(): PrintWriter {
        return this.writer
    }

    @Throws(IOException::class)
    override fun flushBuffer() {
        this.out.flush()
        this.writer.flush()
    }

    override fun addCookie(cookie: Cookie?) {
        super.addCookie(cookie)
    }

    override fun addHeader(name: String?, value: String?) {
        super.addHeader(name, value)
    }


    /**
     * 只能调用一次,set调用之后,就会关闭流.
     */
    var result: ByteArray
        @Throws(IOException::class)
        get() {
            flushBuffer()

            var result: ByteArray  = this.out.bos.toByteArray()
            if (result.size == 0) return ByteArray(0)

            if (result[0].toInt() == 65279) {
                result = Arrays.copyOfRange(result, 1, result.size - 1)
            }
            return result
        }
        @Throws(IOException::class)
        set(result) {
            if (result.size == 0) return

            val response = getResponse()
            response.setContentLength(result.size)
            val sos = response.outputStream
            sos.write(result)
            sos.flush()
            sos.close()
        }

    @Throws(Throwable::class)
    fun finalize() {
        this.writer.close()
        this.out.close()
    }

    override fun isCommitted(): Boolean {
        return super.isCommitted()
    }

    override fun getResponse(): HttpServletResponse {
        return this.response!!
    }

//    fun setResponse(@NotNull response: HttpServletResponse) {
//        this.response = response
//    }

    init {
        this.out = ByteServletOutputStream()
        this.writer = PrintWriter(this.out as OutputStream)
    }


    inner class ByteServletOutputStream : ServletOutputStream() {


        var bos: ByteArrayOutputStream  = ByteArrayOutputStream()

        override fun isReady(): Boolean {
            return true
        }

        override fun setWriteListener( p0: WriteListener) {}

        @Throws(IOException::class)
        override fun write(b: Int) {
            this.bos.write(b)
        }

        @Throws(IOException::class)
        override fun flush() {
            this.bos.flush()
        }

        @Throws(IOException::class)
        override fun close() {
            this.bos.close()
        }
    }
}