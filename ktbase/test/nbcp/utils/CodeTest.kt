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
        val sect = MyUtil.getBigCamelCase("AbcDefXyz").replace(Regex("[A-Z]"), {
            if(it.range.first == 0){
                return@replace it.value.lowercase()
            }
            return@replace "-" + it.value.lowercase()
        })

        println(sect)
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