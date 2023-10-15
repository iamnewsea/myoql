package nbcp.web.sys.handler


import nbcp.base.exception.NoDbDataException
import nbcp.base.exception.ParameterInvalidException
import nbcp.base.comm.const
import nbcp.base.extend.HasValue
import nbcp.base.extend.*
import nbcp.base.utils.*
import nbcp.mvc.sys.WriteTextValue
import nbcp.mvc.sys.findParameterIntValue
import nbcp.mvc.sys.findParameterStringValue
import nbcp.mvc.annotation.*
import nbcp.myoql.db.db
import nbcp.myoql.model.IUploadFileDbService
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * 动态获取图片，参数：
 * id,url: 两者必传其一
 * width,height
 */
@OpenAction
@RestController
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(db::class)
open class ImageGetServlet {

    private val dbService by lazy {
        return@lazy SpringUtil.getBean<IUploadFileDbService>()
    }

    @GetMapping("/image/dynamic")
    fun doGet(request: HttpServletRequest, response: HttpServletResponse) {
        //附件数据库表中的Id
        var id = request.findParameterStringValue("id");

        //带 host 头部的地址
        var url = request.findParameterStringValue("url");
        if (id.isEmpty() && url.isEmpty()) {
            throw ParameterInvalidException("参数非法")
        }

        var width = request.findParameterIntValue("width")
        var height = request.findParameterIntValue("height")

        if (width <= 0 && height <= 0) {
            throw ParameterInvalidException("参数不正确")
//            response.status = 500;
//            response.WriteTextValue("参数不正确")
//            return;
        }

        if (url.isEmpty()) {
            url = dbService.queryById(id)?.url ?: throw NoDbDataException("找不到数据")
        } else {
            url = UrlUtil.decodeURIComponent(url);
        }

        var ret = ImageUtil.zoomImageScale(HttpUtil.getImage(url).inputStream(), response.outputStream, width, height)
        if (ret.msg.HasValue) {
            response.WriteTextValue(ret.msg)
            return;
        }


        var contentType = WebUtil.getMimeType(ret.data!!)
        if (contentType.HasValue) {
            response.contentType = contentType
        }
        response.outputStream.write(ret.ToJson().toByteArray(const.utf8));
    }
}