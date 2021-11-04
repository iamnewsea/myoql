package nbcp.base.handler

import nbcp.db.IdUrl
import nbcp.comm.*
import nbcp.db.mongo.*
import nbcp.web.findParameterIntValue
import nbcp.web.findParameterStringValue
import nbcp.web.findParameterValue
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@WebServlet(urlPatterns = ["/image/change"])
open class ImageChangeServlet : HttpServlet() {
    override fun doPost(request: HttpServletRequest, resp: HttpServletResponse) {
        var action = request.findParameterStringValue("action").ToEnum(MongoImageActionEnum::class.java)!!
        var db = request.findParameterStringValue("db")
        var id = request.findParameterStringValue("id")

        var image = (request.findParameterValue("image")?.ConvertType(IdUrl::class.java) as IdUrl?) ?: IdUrl()
        var index1 = request.findParameterIntValue("index1").AsInt()
        var index2 = request.findParameterIntValue("index2").AsInt()

        var res = proc(action, db, id, image, index1, index2);
        resp.contentType = "application/json;charset=UTF-8"
        resp.writer.write(res.ToJson())
    }

    fun proc(action: MongoImageActionEnum, db: String, id: String, image: IdUrl, index1: Int, index2: Int): JsonResult {
        if (db.isEmpty()) {
            return JsonResult("缺少 db 定义")
        }

        var dbs = db.split(".");
        if (dbs.size < 2) {
            return JsonResult("db 非法")
        }

        var table = dbs[0];
        var field = dbs.Skip(1).joinToString(".")
        var collection = MongoEntityCollector.getCollection(table);
        if (collection == null) {
            return JsonResult("找不到集合")
        }
        return collection.imageChange(action, field, id, image, index1, index2)
    }
}