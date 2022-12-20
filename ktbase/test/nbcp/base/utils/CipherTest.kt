package nbcp.base.utils

import nbcp.base.TestBase
import org.junit.jupiter.api.Test

class CipherTest : TestBase() {
    @Test
    fun test() {
        var salt = "kac2k4h7ezg";
        println(CipherUtil.sha1("123.456" + CipherUtil.sha1(salt)))
        //edcbf84c4b889acd4332d3121afc66b0a83752f5
        //edcbf84c4b889acd4332d3121afc66b0a83752f5
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