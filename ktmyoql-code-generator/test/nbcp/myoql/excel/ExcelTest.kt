package nbcp.myoql.excel

import nbcp.base.comm.JsonMap
import nbcp.base.extend.AsString
import nbcp.base.extend.ToJson
import nbcp.myoql.TestBase
import nbcp.myoql.db.excel.ExcelComponent
import org.junit.jupiter.api.Test
import java.io.FileInputStream
import java.io.FileOutputStream
import java.time.LocalDateTime

class ExcelTest : TestBase() {
    @Test
    fun abc() {
        var excel = ExcelComponent { FileInputStream("""/home/udi/Desktop/a.xlsx""") }
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
            it.put("备注", LocalDateTime.now().AsString())
        }
        t.rows.add(JsonMap("账户名" to "aafa"))


        FileOutputStream("""/home/udi/Desktop/a1.xlsx""").use {
            sheet.writeData(it, t)
        }


    }
}