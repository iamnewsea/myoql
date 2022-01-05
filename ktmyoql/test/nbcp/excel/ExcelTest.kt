package nbcp.excel

import nbcp.TestBase
import nbcp.comm.AsString
import nbcp.comm.JsonMap
import nbcp.comm.ToJson
import nbcp.db.excel.ExcelComponent
import nbcp.db.mongo.MongoColumnName
import org.junit.jupiter.api.Test
import java.io.FileInputStream
import java.io.FileOutputStream
import java.time.LocalDateTime

class ExcelTest : TestBase() {
    @Test
    fun abc() {
        var excel = ExcelComponent(FileInputStream("""/home/udi/Desktop/a.xlsx"""));
        println(excel.sheetNames.joinToString())


        var columns = listOf<String>(
            "姓名",
            "工号",
            "部门",
            "用户组",
            "岗位",
            "发送密码方式（手机/邮箱）",
            "入职时间",
            "员工类型",
            "员工状态",
            "账户名",
            "手机",
            "邮箱",
            "备注"
        )
        var sheet = excel.select(excel.sheetNames.first())
            .setStrictMode(false)
            .setColumns(*columns.toTypedArray())
            .setRowOffset(1)
            .setPks("账户名")

        var t = sheet.getDataTable(JsonMap::class.java)



        t.rows.forEach {
            println(it.ToJson())
        }


        t.rows.forEach {
            it.put("姓名", LocalDateTime.now().AsString())
        }

        sheet.setColumns("a", "b", "c")


        sheet.writeData(FileOutputStream("""/home/udi/Desktop/用户模板2.xlsx"""), 0, t)

    }
}