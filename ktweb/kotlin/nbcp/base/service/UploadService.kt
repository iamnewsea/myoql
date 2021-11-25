package nbcp.base.service

import nbcp.comm.*
import nbcp.db.IdName
import nbcp.db.mongo.entity.SysAnnex
import nbcp.model.IUploadFileDbService
import nbcp.base.util.VideoUtil
import nbcp.utils.CodeUtil
import nbcp.utils.SpringUtil
import nbcp.web.findParameterStringValue
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest
import java.io.InputStream
import javax.imageio.ImageIO
import javax.servlet.http.HttpServletRequest


/**
 * 参数传递过程中,都没有 uploadPath 部分.
 */
@Service
open class UploadService {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }


    @Value("\${app.upload.group-corp:true}")
    private var UPLOAD_GROUPCORP = false

//    @Value("\${app.upload.group:}")
//    private var UPLOAD_GROUP = ""


    private fun getFileInfo(fileName: String): FileExtentionInfo {
        if (!fileName.contains('.')) {
            return FileExtentionInfo("")
        }

        var fileNameValue = fileName;
        fileNameValue = fileNameValue.Remove("/", "\\", "?", "#", "\"", "'", " ", "%", "&", ":", "@", "<", ">")
        var extInfo = FileExtentionInfo(fileNameValue);

        if (fileNameValue.length < 4) {
            extInfo.name = "";
        }
        return extInfo;
    }

    /**
     * @param vTempFile , 相对于 uploadPath 的相对路径.
     */
    private fun doUpload(
            group: String,
            file: MultipartFile,
            fileName: String,
            storageType: UploadStorageTypeEnum?,
            user: IdName,
            corpId: String
    ): ApiResult<SysAnnex> {
        var fileStream = file.inputStream;

        var extInfo = getFileInfo(fileName)
        if (extInfo.name.isEmpty()) {
            extInfo.name = CodeUtil.getCode()
        }

        var fileData = UploadFileNameData();
        fileData.fileName = extInfo.getFileName()
        fileData.extName = extInfo.extName
        fileData.extType = extInfo.extType
        fileData.groupCorp = UPLOAD_GROUPCORP;
        fileData.corpId = corpId;


        var annexInfo = SysAnnex();
        annexInfo.ext = extInfo.extName
        annexInfo.size = file.size.toInt()
        annexInfo.creator = user
        annexInfo.group = group
        annexInfo.corpId = corpId


        //流不可重复读！！所以要重新从file中获取流
        if (extInfo.extType == FileExtentionTypeEnum.Image) {
            val bufferedImage = ImageIO.read(file.inputStream)
            annexInfo.imgWidth = bufferedImage.getWidth();
            annexInfo.imgHeight = bufferedImage.getHeight();

        } else if (extInfo.extType == FileExtentionTypeEnum.Video) {
            VideoUtil.getVideoInfo(file.inputStream).data.apply {
                if (this != null) {
                    annexInfo.imgWidth = this.width;
                    annexInfo.imgHeight = this.height;
                    annexInfo.videoTime = this.time;


                    var logoFile = UploadFileNameData();
                    logoFile.corpId = fileData.corpId;
                    logoFile.extName = "png";
                    logoFile.extType = FileExtentionTypeEnum.Image;
                    logoFile.groupCorp = fileData.groupCorp;
                    logoFile.fileName = CodeUtil.getCode() + ".png";

                    var logoUrl = saveFile(this.logoStream, annexInfo.group, logoFile, storageType);
                    annexInfo.videoLogoUrl = logoUrl
                }
            }

        }

//        fileData.imgWidth = annexInfo.imgWidth;
//        fileData.imgHeight = annexInfo.imgHeight;

        annexInfo.url = saveFile(fileStream, annexInfo.group, fileData, storageType).replace("\\", "/")

        annexInfo.corpId = corpId

        if (dbService.insert(annexInfo) == 0) {
            return ApiResult.error("记录到数据出错")
        }
        return ApiResult.of(annexInfo)
    }

//    private fun setVideoUrlTime(annexInfo: SysAnnex, vFile: File, fileData: FileNameData) {
//        var targetFileName = fileData.getTargetPaths().joinToString(File.separator) + File.separator + CodeUtil.getCode() + annexInfo.ext;
//        annexInfo.videoLogoUrl = VideoUtil.getVideoLogo(vFile, uploadPath, targetFileName)
//    }


    @Autowired
    lateinit var localUploader: UploadFileForLocalService

    val minioUploader: UploadFileForMinioService? by lazy {
        return@lazy SpringUtil.getBeanWithNull(UploadFileForMinioService::class.java)
    }

    val aliOssUploader: UploadFileForAliOssService? by lazy {
        return@lazy SpringUtil.getBeanWithNull(UploadFileForAliOssService::class.java)
    }

    /**
     * 把文件转移到相应的文件夹下.
     * 1. 第一级目录,按 年-月 归档.
     * 2. 第二级目录,按 企业Id 归档.
     *      2.1.如果是后台, 企业Id = admin
     *      2.2.如果是商城用户,企业Id = shop
     * 3. 第三级目录,是 后缀名
     * 4. 如果是图片，第四级目录是原图片的像素数/万 ，如 800*600 = 480000,则文件夹名为 48 。 忽略小数部分。这样对大部分图片大体归类。
     */
    fun saveFile(
            fileStream: InputStream,
            group: String,
            fileData: UploadFileNameData,
            storageType: UploadStorageTypeEnum?
    ): String {
        var storageTypeValue = storageType;

        if (storageTypeValue == null) {
            if (aliOssUploader?.check() == true) {
                storageTypeValue = UploadStorageTypeEnum.AliOss
            } else if (minioUploader?.check() == true) {
                storageTypeValue = UploadStorageTypeEnum.Minio
            } else if (localUploader.check()) {
                storageTypeValue = UploadStorageTypeEnum.Local
            }
        }

        if (storageTypeValue == UploadStorageTypeEnum.AliOss) {
            return aliOssUploader!!.upload(fileStream, group, fileData)
        }

        if (storageTypeValue == UploadStorageTypeEnum.Minio) {
            return minioUploader!!.upload(fileStream, group, fileData)
        }

        if (storageTypeValue == UploadStorageTypeEnum.Local) {
            return localUploader.upload(fileStream, group, fileData)
        }


        throw java.lang.RuntimeException("请配置上传方式，MinIO方式：app.upload.minio.endpoint,app.upload.minio.accessKey; 本地文件方式：app.upload.local.host,app.upload.local.path")
    }


    private val dbService by lazy {
        return@lazy SpringUtil.getBean<IUploadFileDbService>()
    }

    /**
     * 文件上传
     */
    fun upload(
            request: HttpServletRequest,
            user: IdName,
            corpId: String,
    ): ListResult<SysAnnex> {
        var list = mutableListOf<SysAnnex>()

        var msg = ""
        var group = request.findParameterStringValue("group")
        var storageType = request.findParameterStringValue("storage-type").ToEnum<UploadStorageTypeEnum>()

        (request as StandardMultipartHttpServletRequest)
                .multiFileMap
                .toList()
                .ForEachExt { it, _ ->
                    var fileName = it.first;
                    var files = it.second;

                    files.ForEachExt for2@{ file, _ ->
                        var ret1 = doUpload(group, file, fileName, storageType, user, corpId);
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
            return ListResult(msg);
        }

        return ListResult.of(list)
    }
}