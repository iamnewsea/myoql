package nbcp.mvc.dev2

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.web.bind.annotation.RestController
import nbcp.comm.*
import nbcp.db.*
import nbcp.db.mongo.*
import nbcp.db.mongo.entity.*
import nbcp.web.*
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.annotation.RequestMapping
import javax.servlet.http.HttpServletRequest
import java.time.*

/**
 * Created by CodeGenerator at 2021-04-11 23:42:19
 */
@Api(description = "数据连接", tags = arrayOf("DbConnection"))
@RestController
@RequestMapping("/dev")
class DbConnectionAutoController {

    @PostMapping("/test")
    fun test1() {
        var result = db.mor_base.sysAnnex.aggregate()
            .addPipeLineRawString(PipeLineEnum.match, """ { "group" : "lowcode"} """.replace("##", "$"))
            .addPipeLineRawString(
                PipeLineEnum.group, """
{
    _id: { 扩展名: "##ext" },
    总数: { ##sum : 1 },
    最小: { ##min: "##size" } ,
    最大: { ##max: "##size" }
}
            """.replace("##", "$")
            )
            .addPipeLineRawString(PipeLineEnum.sort, """ { "_id.扩展名":1 } """)
            .toMapList()

        /**
         * 生成的语句：
         *
         *
         * 返回的结果：
         * [{"总数":1,"最小":26821,"最大":26821,"id":"Document{{扩展名=abc}}"},{"总数":5,"最小":5229,"最大":170276,"id":"Document{{扩展名=png}}"}]
         */

        println("OK")

    }
}


