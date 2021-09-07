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
    fun test1(): ApiResult<String> {
        return ApiResult()
    }
}


