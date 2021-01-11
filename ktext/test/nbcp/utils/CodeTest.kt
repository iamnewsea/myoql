package nbcp.utils

import nbcp.TestBase
import nbcp.comm.*
import nbcp.component.SnowFlake
import org.junit.Test
import java.io.File
import java.time.format.DateTimeFormatter

class CodeTest : TestBase() {
    @Test
    fun test_code() {
        var sect = MyUtil.getBigCamelCase("AbcDefXyz").replace(Regex("[A-Z]"), {
            if(it.range.first == 0){
                return@replace it.value.toLowerCase()
            }
            return@replace "-" + it.value.toLowerCase()
        })

        println(sect)
    }

    @Test
    fun ag() {
        ZipUtil.listFile(File("""d:\opt\nginx-1.17.6.zip"""), "").forEach {
            if (it.isDirectory) {
                println("[" + it.fileName + "]")
            } else {
                println(it.fileName)
            }
        }
    }
}