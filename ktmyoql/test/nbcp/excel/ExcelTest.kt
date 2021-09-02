package nbcp.excel

import nbcp.TestBase
import nbcp.comm.AsString
import nbcp.comm.JsonMap
import nbcp.comm.ToJson
import nbcp.db.excel.ExcelComponent
import org.junit.Test
import java.io.FileInputStream
import java.io.FileOutputStream
import java.time.LocalDateTime

class ExcelTest : TestBase() {
    @Test
    fun abc() {
        var excel = ExcelComponent { FileInputStream("""d:\a.xlsx""") };
        println(excel.sheetNames.joinToString())


        var sheet = excel.select("Sheet1")
            .setStrictMode(false)
            .setColumns("a", "b")

        var t = sheet.getDataTable(JsonMap::class.java)



        t.rows.forEach {
            println(it.ToJson())
        }


        t.rows.forEach {
            it.put("b", LocalDateTime.now().AsString())
        }

        sheet.setColumns("a", "b", "c")


        sheet.writeData(FileOutputStream("""d:\b.xlsx"""), 0, t)

    }
}