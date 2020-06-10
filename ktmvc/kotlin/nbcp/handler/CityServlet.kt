package nbcp.handler

import nbcp.db.IdUrl
import nbcp.comm.*
import nbcp.db.*
import nbcp.utils.*
import nbcp.db.DatabaseEnum
import nbcp.db.db
import nbcp.db.mongo.*
import nbcp.db.mongo.service.UploadFileMongoService
import nbcp.db.mysql.service.UploadFileMysqlService
import nbcp.web.MyHttpRequestWrapper
import nbcp.web.WriteJsonRawValue
import nbcp.web.WriteTextValue
import nbcp.web.findParameterValue
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import java.io.File
import java.lang.RuntimeException
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * 获取城市下级数据
 */
data class cn_city_model(var c: Int, var n: String)

@OpenAction
@WebServlet(urlPatterns = arrayOf("/child-citys"))
open class CityServlet : HttpServlet() {
    override fun doPost(request: HttpServletRequest, response: HttpServletResponse) {

        var pcode = request.findParameterValue("pcode").AsInt();

        if (pcode == 0) {
            throw NoDataException("城市code不能为空")
        }

        if (pcode % 100 != 0) {
            throw ParameterInvalidException("城市code非法", "pcode")
        }

        var list = db.mor_base.sysCity.query()
                .where { it.pcode match pcode }
                .toList(IntCodeName::class.java)
                .map { cn_city_model(it.code, it.name) }

        response.WriteJsonRawValue(ApiResult.of(list).ToJson())
    }
}