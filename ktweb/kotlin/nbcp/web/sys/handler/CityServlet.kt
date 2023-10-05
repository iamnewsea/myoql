package nbcp.web.sys.handler

import nbcp.base.annotation.Require
import nbcp.base.comm.ListResult
import nbcp.base.exception.NoDbDataException
import nbcp.base.exception.ParameterInvalidException
import nbcp.base.extend.AsInt
import nbcp.mvc.annotation.*
import nbcp.myoql.db.db
import nbcp.myoql.db.mongo.query
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletResponse

/**
 * 获取城市下级数据
 */
data class cn_city_model(var c: Int, var n: String)

@OpenAction
@RestController
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(value = arrayOf(MongoTemplate::class,  db::class))
open class CityServlet {
    @PostMapping("/open/child-cities")
    fun child(@Require pcode: Int, response: HttpServletResponse): ListResult<cn_city_model> {
        if (pcode % 100 != 0) {
            throw ParameterInvalidException("城市code非法")
        }

        var list = db.morBase.sysCity.query()
            .select { it.code }
            .select { it.shortName }
            .where { it.pcode mongoEquals pcode }
            .toList()
            .map { cn_city_model(it.code, it.shortName) }

        return ListResult.of(list)
    }

    /**
     * 返回 wbs 路径信息
     */
    @PostMapping("/open/city-full-info")
    fun fullInfo(@Require code: Int, response: HttpServletResponse): ListResult<cn_city_model> {
        if (code == 0) {
            throw NoDbDataException("城市code不能为空")
        }

        var city_codes = getWbsCodes(code)

        var list = db.morBase.sysCity.query()
            .select { it.code }
            .select { it.shortName }
            .where { it.code mongoIn city_codes }
            .orderByAsc { it.code }
            .toList()
            .map { cn_city_model(it.code, it.shortName) }

        return ListResult.of(list)
    }

    private fun getWbsCodes(code: Int): List<Int> {
        var codeString = code.toString();
        return setOf<String>(
            codeString.substring(0..1) + "0000",
            codeString.substring(0..3) + "00",
            codeString.substring(0..5),
        ).map { it.AsInt() };
    }
}