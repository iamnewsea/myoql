package nbcp.excel

import nbcp.TestBase
import nbcp.comm.JsonMap
import nbcp.comm.ToJson
import nbcp.db.excel.ExcelComponent
import org.junit.Test
import java.io.FileInputStream

class ExcelTest : TestBase() {
    @Test
    fun abc() {
        var excel = ExcelComponent { FileInputStream("""d:\a.xlsx""") };
        println(excel.sheetNames.joinToString())

        excel.select("Sheet1")
            .setStrictMode(false)
            .setColumns("a", "b")

            .getDataTable(JsonMap::class.java)
            .rows.forEach {
                println(it.ToJson())
            }
    }
}