package nbcp.handler

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import nbcp.comm.*
import nbcp.db.*
import nbcp.scope.JsonSceneEnumScope
import nbcp.service.UploadService
import nbcp.web.*
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Created by udi on 17-4-12.
 */


/**
 * 客户端上传流程：
 *  1. 如果需要上传，客户端调用 /sys/upload 方法，返回文件Id
 *  2. 客户端调用 业务方法，把文件Id和业务关联。
 */
@RestController
@ConditionalOnBean(UploadService::class)
class FileUploadController {
    @Autowired
    lateinit var uploadService: UploadService;

    /**
     * 文件上传流程：
     *  1. 保存文件
     *  2. 计算文件 Md5 值
     *  3. 插入到 SysAnnex 表中。
     *  4. 返回 SysAnnex.id
     */
    @PostMapping("/sys/upload")
    fun fileUpload(request: HttpServletRequest, response: HttpServletResponse) {
        if (request is StandardMultipartHttpServletRequest == false) {
            throw RuntimeException("request非StandardMultipartHttpServletRequest类型")
        }

        var ret =
            uploadService.upload(request, IdName(request.UserId, request.UserName), request.LoginUser.organization.id);

        if (ret.msg.HasValue) {
            response.WriteJsonRawValue(JsonResult(ret.msg).ToJson())
            return;
        }

        var ids = ret.data;

        response.contentType = "application/json;charset=UTF-8"
        usingScope(JsonSceneEnumScope.Web) {
            if (ids.size == 0) {
                response.outputStream.write(JsonResult("上传失败").ToJson().toByteArray(const.utf8));
            } else if (ids.size == 1) {
                response.outputStream.write(ApiResult.of(ids[0]).ToJson().toByteArray(const.utf8));
            } else {
                response.outputStream.write(ListResult.of(ids).ToJson().toByteArray(const.utf8));
            }
        }
        return
    }
}
