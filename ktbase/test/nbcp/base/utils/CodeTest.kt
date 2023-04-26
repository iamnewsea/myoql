package nbcp.base.utils

import nbcp.base.TestBase
import org.junit.jupiter.api.Test
import org.springframework.util.AntPathMatcher

class CodeTest : TestBase() {
    @Test
    fun test_code() {
        var match = AntPathMatcher(".");

        println(match.match("appDeploySetting.%2A%2A","appDeploySetting.abc"))
        println(match.match("appDeploySetting.*",null))

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