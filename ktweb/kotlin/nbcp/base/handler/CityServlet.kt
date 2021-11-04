package nbcp.base.handler

import nbcp.comm.*
import nbcp.db.*
import nbcp.utils.*
import nbcp.db.db
import nbcp.db.mongo.*
import nbcp.web.WriteJsonRawValue
import nbcp.web.findParameterIntValue
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * 获取城市下级数据
 */
data class cn_city_model(var c: Int, var n: String)


@WebServlet(urlPatterns = ["/child-citys"])
open class CityServlet : HttpServlet() {
    override fun doPost(request: HttpServletRequest, response: HttpServletResponse) {

        var pcode = request.findParameterIntValue("pcode") ;

        if (pcode == 0) {
            throw NoDataException("城市code不能为空")
        }

        if (pcode % 100 != 0) {
            throw ParameterInvalidException("城市code非法")
        }

        var list = db.mor_base.sysCity.query()
                .select { it.code }
                .select { it.shortName }
                .where { it.pcode match pcode }
                .toList()
                .map { cn_city_model(it.code, it.shortName) }

        response.WriteJsonRawValue(ApiResult.of(list).ToJson())
    }
}