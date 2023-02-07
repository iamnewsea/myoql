package nbcp.web.service.upload

import nbcp.base.comm.*
import nbcp.base.db.*
import nbcp.base.enums.*
import nbcp.base.extend.*
import nbcp.base.utils.*
import nbcp.mvc.mvc.WriteJsonRawValue
import nbcp.mvc.mvc.findParameterStringValue
import nbcp.myoql.db.mongo.entity.SysAnnex
import nbcp.myoql.model.IUploadFileDbService
import nbcp.web.*
import nbcp.web.extend.*
import nbcp.web.util.VideoUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest
import java.io.InputStream
import javax.imageio.ImageIO
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * 参数传递过程中,都没有 uploadPath 部分.
 */
abstract class BaseUploadService {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }


    /**
     * @param vTempFile , 相对于 uploadPath 的相对路径.
     */
    fun uploadRequestFile(
        file: MultipartFile,
        group: String,
        fileName: String,
        user: IdName,
        corpId: String
    ): nbcp.base.comm.ApiResult<SysAnnex> {
        var storageType = config.getConfig("app.upload.storageType").AsString("")

        var fileStream = file.inputStream;

        var extInfo = FileExtensionInfo.ofFileName(fileName)
        if (extInfo.name.isEmpty()) {
            extInfo.name = CodeUtil.getCode()
        }

        var fileData = UploadFileNameData();
        fileData.fileName = extInfo.getFileName()
        fileData.extName = extInfo.extName
        fileData.extType = extInfo.extType
        fileData.corpId = corpId;


        var annexInfo = SysAnnex();
        annexInfo.name = fileData.fileName;
        annexInfo.storageType = storageType;
        annexInfo.ext = extInfo.extName
        annexInfo.size = file.size.toInt()
        annexInfo.creator = user
        annexInfo.group = group
        annexInfo.corpId = corpId


        //流不可重复读！！所以要重新从file中获取流
        if (extInfo.extType == FileExtensionTypeEnum.IMAGE) {
            try {
                val bufferedImage = ImageIO.read(file.inputStream)
                annexInfo.imgWidth = bufferedImage.getWidth();
                annexInfo.imgHeight = bufferedImage.getHeight();
            } catch (ex: Throwable) {
                return ApiResult.error("不识别的图片格式!")
            }
        } else if (extInfo.extType == FileExtensionTypeEnum.VIDEO) {
            if (config.videoLogo) {
                VideoUtil.getVideoInfo(file.inputStream).data
                    .apply {
                        if (this != null) {
                            annexInfo.imgWidth = this.width;
                            annexInfo.imgHeight = this.height;
                            annexInfo.videoTime = this.time;


                            var logoFile = UploadFileNameData();
                            logoFile.corpId = fileData.corpId;
                            logoFile.extName = "png";
                            logoFile.extType = FileExtensionTypeEnum.IMAGE;
                            logoFile.fileName = CodeUtil.getCode() + ".png";

                            var logoUrl = saveFile(storageType, logoStream, annexInfo.group, logoFile);
                            annexInfo.videoLogoUrl = logoUrl
                        }
                    }
            }
        }

//        fileData.imgWidth = annexInfo.imgWidth;
//        fileData.imgHeight = annexInfo.imgHeight;
        annexInfo.corpId = corpId

        annexInfo.url = saveFile(storageType, fileStream, annexInfo.group, fileData)

        if (annexInfo.url.isEmpty()) {
            return ApiResult.error("保存文件出错")
        }


        if (dbService.insert(annexInfo) == 0) {
            return ApiResult.error("记录到数据出错")
        }
        return ApiResult.of(annexInfo)
    }

//    private fun setVideoUrlTime(annexInfo: SysAnnex, vFile: File, fileData: FileNameData) {
//        var targetFileName = fileData.getTargetPaths().joinToString(File.separator) + File.separator + CodeUtil.getCode() + annexInfo.ext;
//        annexInfo.videoLogoUrl = VideoUtil.getVideoLogo(vFile, uploadPath, targetFileName)
//    }


    /**
     * 把文件转移到相应的文件夹下.
     * 1. 第一级目录,按 年-月 归档.
     * 2. 第二级目录,按 企业Id 归档.
     *      2.1.如果是后台, 企业Id = admin
     *      2.2.如果是商城用户,企业Id = shop
     * 3. 第三级目录,是 后缀名
     * 4. 如果是图片，第四级目录是原图片的像素数/万 ，如 800*600 = 480000,则文件夹名为 48 。 忽略小数部分。这样对大部分图片大体归类。
     * @return 返回网络可访问地址。
     */
    abstract fun saveFile(
        storageType: String,
        fileStream: InputStream,
        group: String,
        fileData: UploadFileNameData,
    ): String


    @Autowired
    lateinit var dbService: IUploadFileDbService


    /**
     * 文件上传
     * 头格式：  Content-Disposition: form-data; name="多文件上传时可能是多个文件名，或空"; filename="具体文件名"
     */
    protected open fun upload(
        request: HttpServletRequest,
        response: HttpServletResponse,
        group: String
    ) {

        if (request is StandardMultipartHttpServletRequest == false) {
            throw RuntimeException("request非StandardMultipartHttpServletRequest类型")
        }

        val ret = uploadRequest(
            request,
            group.AsString { request.findParameterStringValue("group") },
            IdName(request.UserId, request.UserName),
            request.LoginUser.organization.id
        );

        if (ret.msg.HasValue) {
            response.WriteJsonRawValue(JsonResult.error(ret.msg).ToJson())
            return;
        }

        val ids = ret.data;

        response.contentType = "application/json;charset=UTF-8"
        usingScope(JsonSceneScopeEnum.WEB) {
            if (ids.size == 0) {
                response.outputStream.write(JsonResult.error("上传失败").ToJson().toByteArray(const.utf8));
            } else if (ids.size == 1) {
                response.outputStream.write(ApiResult.of(ids[0]).ToJson().toByteArray(const.utf8));
            } else {
                response.outputStream.write(ListResult.of(ids).ToJson().toByteArray(const.utf8));
            }
        }
        return
    }

//    class AnnexResult(id: String = "", var name: String = "", url: String = "", var videoLogUrl: String? = null) :
//        IdUrl(id, url)

    /**
     * 文件上传
     */
    private fun uploadRequest(
        request: StandardMultipartHttpServletRequest,
        group: String,
        user: IdName,
        corpId: String,
    ): ListResult<SysAnnex> {
        var list = mutableListOf<SysAnnex>()

        var msg = ""

        request
            .multiFileMap
            .toList()
            .apply {
                if (this.any() == false) {
                    throw RuntimeException("找不到上传的文件!")
                }
            }
            .ForEachExt { it, _ ->
                var fileName = it.first;
                var files = it.second;

                files.ForEachExt for2@{ file, _ ->
                    fileName = getBestFileName(file.originalFilename, fileName)
                    var ret1 = uploadRequestFile(file, group, fileName, user, corpId);
                    if (ret1.msg.HasValue) {
                        msg = ret1.msg;
                        return@ForEachExt false
                    }
                    list.add(ret1.data!!);
                    return@for2 true
                }

                return@ForEachExt true;
            }


        if (msg.HasValue) {
            return ListResult.error(msg);
        }

        return ListResult.of(list)
    }

    /**
     * 找到最合适的文件名
     */
    fun getBestFileName(fileName: String?, originalFilename: String?): String {
        if (fileName.isNullOrEmpty() && originalFilename.isNullOrEmpty()) {
            throw RuntimeException("找不到文件名")
        }
        if (fileName.isNullOrEmpty()) {
            return originalFilename!!
        }
        if (originalFilename.isNullOrEmpty()) return fileName;

        if (fileName == "blob") {
            return originalFilename.AsString()
        }

        if (fileName.contains(".")) {
            return fileName
        }

        return originalFilename;
    }
}