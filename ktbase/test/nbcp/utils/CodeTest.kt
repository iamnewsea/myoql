package nbcp.utils

import nbcp.TestBase
import nbcp.comm.*
import nbcp.component.SnowFlake
import org.junit.jupiter.api.Test
import java.io.File
import java.time.format.DateTimeFormatter

class CodeTest : TestBase() {
    @Test
    fun test_code() {
        var e = CipherUtil.AESUtil.generateKey();
        println(e)
        var r = CipherUtil.AESUtil.encrypt("abc", e)
        println(r)
        println(CipherUtil.AESUtil.decrypt(r, e))
    }

//    @Test
//    fun ag() {
//        ZipUtil.listFile(File("""d:\opt\nginx-1.17.6.zip"""), "").forEach {
//            if (it.isDirectory) {
//                println("[" + it.fileName + "]")
//            } else {
//                println(it.fileName)
//            }
//        }
//    }
}