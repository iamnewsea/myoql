package nbcp.base.handler

import nbcp.db.IdUrl
import nbcp.comm.*

import nbcp.db.mongo.*
import nbcp.web.findParameterStringValue
import nbcp.web.findParameterValue
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController


import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@RestController
open class ImageServlet {
    @PostMapping("/image/set")
    fun doPost(request: HttpServletRequest, resp: HttpServletResponse) {
        var db = request.findParameterStringValue("db")
        var id = request.findParameterStringValue("id")

        var image = (request.findParameterValue("image")?.ConvertType(IdUrl::class.java) as IdUrl?) ?: IdUrl()

        var ret = proc(db, id, image);


        resp.contentType = "application/json;charset=UTF-8"
        resp.outputStream.write(ret.ToJson().toByteArray(const.utf8));
    }

    fun proc(db: String, id: String, image: IdUrl): JsonResult {
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
        return collection.imageSet(field, id, image)
    }
}