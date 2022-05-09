package nbcp.base.mvc.handler

import nbcp.db.IdUrl
import nbcp.comm.*

import nbcp.db.mongo.*
import nbcp.base.mvc.*
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController


import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@RestController
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
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
            return JsonResult.error("缺少 db 定义")
        }

        var dbs = db.split(".");
        if (dbs.size < 2) {
            return JsonResult.error("db 非法")
        }

        var table = dbs[0];
        var field = dbs.Skip(1).joinToString(".")
        var collection = nbcp.db.db.mongo.dynamicEntity(table);

        return collection.imageSet(field, id, image)
    }
}