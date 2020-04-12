package nbcp.handler

import nbcp.db.IdUrl
import nbcp.comm.*

import nbcp.db.mongo.imageSet
import nbcp.db.mongo.*
import nbcp.web.MyHttpRequestWrapper
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@WebServlet(urlPatterns = arrayOf("/image/set"))
open class ImageServlet : HttpServlet() {
    override fun doPost(req: HttpServletRequest?, resp: HttpServletResponse) {
        var ret = JsonResult();

        if (req == null || (req is MyHttpRequestWrapper == false)) ret = JsonResult("非法请求");
        else {
            ret = postJson(req)
        }

        resp.contentType = "application/json;charset=UTF-8"
        resp.outputStream.write(ret.ToJson().toByteArray(utf8));
    }

    private fun postJson(req: MyHttpRequestWrapper): JsonResult {

        var db = req.getValue("db")
        var id = req.getValue("id")

        var imageObj = req.json.get("image");
        if (imageObj == null) {
            return JsonResult("找不到图片信息");
        }
        var image = imageObj.ConvertType(IdUrl::class.java) as IdUrl
//        if (image.id.isNullOrEmpty()) {
//            return JsonResult("找不到图片信息");
//        }
        return proc(db, id, image);
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
        var collection = MongoEntityEvent.getCollection(table);
        if (collection == null) {
            return JsonResult("找不到集合")
        }
        return collection.imageSet(field, id, image)
    }
}