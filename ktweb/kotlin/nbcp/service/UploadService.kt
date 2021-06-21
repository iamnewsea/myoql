package nbcp.service

import io.minio.MinioClient
import nbcp.comm.*
import nbcp.db.DatabaseEnum
import nbcp.db.IdName
import nbcp.db.db
import nbcp.db.mongo.entity.SysAnnex
import nbcp.db.mongo.service.UploadFileMongoService
import nbcp.db.mysql.service.UploadFileMysqlService
import nbcp.utils.CodeUtil
import nbcp.utils.SpringUtil
import org.bytedeco.javacv.FFmpegFrameGrabber
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.time.LocalDate
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

    data class FileNameData(var msg: String = "") {
        var oriName: String = ""
        var extName: String = ""
        var extType: FileExtentionTypeEnum = FileExtentionTypeEnum.Other

        //        var imgWidth: Int = 0
//        var imgHeight: Int = 0
        var needCorp: Boolean = true
        var corpId: String = ""

        /*
     * 把文件转移到相应的文件夹下.
     * 1. 第一级目录,按 年-月 归档.
     * 2. 第二级目录,按 企业Id 归档.
     *      2.1.如果是后台, 企业Id = _admin_
     *      2.2.如果是商城用户,企业Id = _shop_
     * 3. 第三级目录,如果是非图片是 extType , 如果是图片是 宽-高
     * 4. 第四级是,如果是图片,是256宽度像素大小的缩略图.
         */
        private fun getTargetPaths(): Array<String> {
            var list = mutableListOf<String>()
            list.add(LocalDate.now().Format("yyyy-MM"));
//            var pixelTotal = imgWidth * imgHeight;
//
//
//            if (pixelTotal > 0) {
//                var pixel = (pixelTotal / 10000.0).toInt();
//                //按图片像素文件夹命名。不足1万 = 0
//                list.add(pixel.toString())
//            } else {
//                list.add(this.extType.toString())
//            }
            return list.toTypedArray();
        }

        private fun getTargetFileNames(): Array<String> {
            return arrayOf(
                *this.getTargetPaths(),
                CodeUtil.getCode() + (if (extName.HasValue) ("." + extName) else "")
            );
        }

        fun getTargetFileName(): String {
            var targetFileName = mutableListOf<String>()

            if (needCorp && corpId.HasValue) {
                targetFileName.add(corpId);
            }
            targetFileName.addAll(getTargetFileNames())
            return targetFileName.map { File.separator + it }.joinToString("")
        }
    }

    @Value("\${app.upload.saveCorp:true}")
    private var UPLOAD_SAVECORP = false

    @Value("\${app.upload.group:}")
    private var UPLOAD_GROUP = ""

    /**
     * 上传到本地时使用该配置,最后不带 "/"
     */
    @Value("\${app.upload.local.host:}")
    var UPLOAD_LOCAL_HOST: String = ""

    /**
     * 上传到本地时使用该配置,最后不带 "/"
     */
    @Value("\${app.upload.local.path:}")
    var UPLOAD_LOCAL_PATH: String = ""

    @Value("\${app.upload.minio.endpoint:}")
    var MINIO_ENDPOINT: String = ""

    @Value("\${app.upload.minio.key:}")
    var MINIO_ACCESSKEY: String = ""

    @Value("\${app.upload.minio.secret:}")
    var MINIO_SECRETKEY: String = ""


    private fun getFileInfo(fileName1: String): FileExtentionInfo {
        var fileName = "";
        if (fileName1.contains('.')) {
            fileName = fileName1;
        } else {
            return FileExtentionInfo("")
        }

        fileName = fileName.Remove("/", "\\", "?", "#", "\"", "'", " ", "%", "&", ":", "@", "<", ">")
        var extInfo = FileExtentionInfo(fileName);

        if (fileName.length < 4) {
            extInfo.name = "";
        }
        return extInfo;
    }

    /**
     * @param vTempFile , 相对于 uploadPath 的相对路径.
     */
    private fun doUpload(
        request: HttpServletRequest,
        file: MultipartFile,
        fileName: String,
        user: IdName,
        corpId: String
    ): ApiResult<SysAnnex> {
        var fileStream = file.inputStream;

        var extInfo = getFileInfo(fileName)
        if (extInfo.name.isEmpty()) {
            extInfo.name = CodeUtil.getCode()
        }

        var fileData = FileNameData();
        fileData.oriName = extInfo.toString()
        fileData.extName = extInfo.extName
        fileData.extType = extInfo.extType
        fileData.needCorp = request.getHeader("save-corp").AsBooleanWithNull() ?: UPLOAD_SAVECORP;
        fileData.corpId = corpId;

        var annexInfo = SysAnnex();
        annexInfo.ext = extInfo.extName
        annexInfo.size = file.size.toInt()
        annexInfo.creator = user
        annexInfo.group = request.getHeader("group").AsStringWithNull() ?: UPLOAD_GROUP
        annexInfo.corpId = corpId


        //流不可重复读！！
//        if (extInfo.extType == FileExtentionTypeEnum.Image) {
//            val bufferedImage = ImageIO.read(fileStream)
//            annexInfo.imgWidth = bufferedImage.getWidth();
//            annexInfo.imgHeight = bufferedImage.getHeight();
//        } else if (extInfo.extType == FileExtentionTypeEnum.Video) {
//            fillVideoWidthHeight(annexInfo, fileStream);
//        }

//        fileData.imgWidth = annexInfo.imgWidth;
//        fileData.imgHeight = annexInfo.imgHeight;

        annexInfo.url = saveFile(fileStream, annexInfo.group, fileData).replace("\\", "/")

        annexInfo.corpId = corpId

        if (dbService.insert(annexInfo) == 0) {
            return ApiResult("记录到数据出错")
        }
        return ApiResult.of(annexInfo)
    }

    private fun fillVideoWidthHeight(annexInfo: SysAnnex, file: InputStream) {
        FFmpegFrameGrabber(file).use { fFmpegFrameGrabber ->
            fFmpegFrameGrabber.start();
            val ftp = fFmpegFrameGrabber.lengthInFrames

            annexInfo.imgHeight = fFmpegFrameGrabber.imageHeight;
            annexInfo.imgWidth = fFmpegFrameGrabber.imageWidth;
            annexInfo.videoTime = (ftp / fFmpegFrameGrabber.frameRate / 60).AsInt();

            fFmpegFrameGrabber.stop()
        }
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
     */
    fun saveFile(fileStream: InputStream, group: String, fileData: FileNameData): String {
        if (MINIO_ENDPOINT.HasValue && MINIO_ACCESSKEY.HasValue) {
            return saveToMinIo(fileStream, group, fileData)
        } else if (UPLOAD_LOCAL_HOST.HasValue && UPLOAD_LOCAL_PATH.HasValue) {
            return saveToLocal(fileStream, group, fileData)
        }

        throw java.lang.RuntimeException("请配置上传方式，MinIO方式：app.upload.minio.endpoint,app.upload.minio.accessKey; 本地文件方式：app.upload.local.host,app.upload.local.path")
    }

    private fun saveToLocal(fileStream: InputStream, group: String, fileData: FileNameData): String {
        var targetFileName = fileData.getTargetFileName()
        var targetFile = File(listOf(UPLOAD_LOCAL_PATH, group, fileData.getTargetFileName()).filter { it.HasValue }
            .joinToString(File.separator));

        if (targetFile.parentFile.exists() == false) {
            if (targetFile.parentFile.mkdirs() == false) {
                throw java.lang.RuntimeException("创建文件夹失败： ${targetFile.parentFile.FullName}")
            }
        }

        FileOutputStream(targetFile).use {
            if (fileStream.copyTo(it) <= 0) {
                throw java.lang.RuntimeException("保存文件失败： ${targetFile.parentFile.FullName}")
            }
        }
        return UPLOAD_LOCAL_HOST + targetFileName;
    }

    private fun saveToMinIo(fileStream: InputStream, group: String, fileData: FileNameData): String {
        if (!MINIO_ENDPOINT.HasValue || !MINIO_ACCESSKEY.HasValue) {
            return "";
        }

        if (group.isEmpty()) {
            throw java.lang.RuntimeException("minIO需要group值！")
        }

        val minioClient = MinioClient(MINIO_ENDPOINT, MINIO_ACCESSKEY, MINIO_SECRETKEY)
        // bucket 不存在，创建
        if (!minioClient.bucketExists(group)) {
            minioClient.makeBucket(group)
        }

        val fileName = fileData.getTargetFileName()
            .replace(File.separatorChar, '/')

        //类型
        val contentType = fileData.extType.toString()
        //把文件放置MinIo桶(文件夹)

        fileStream.use {
            minioClient.putObject(group, fileName, it, contentType)
        }

        return minioClient.getObjectUrl(group, fileName)
    }

    private val dbService by lazy {
        if (db.mainDatabaseType == DatabaseEnum.Mongo) {
            return@lazy SpringUtil.context.getBean(UploadFileMongoService::class.java)
        } else {
            return@lazy SpringUtil.context.getBean(UploadFileMysqlService::class.java)
        }
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
        if (request is StandardMultipartHttpServletRequest == false) {
            throw RuntimeException("request非StandardMultipartHttpServletRequest类型")
        }

        var msg = ""

        (request as StandardMultipartHttpServletRequest)
            .multiFileMap
            .toList()
            .ForEachExt { it, _ ->
                var fileName = it.first;
                var files = it.second;

                files.ForEachExt for2@{ file, _ ->
                    var ret1 = doUpload(request, file, fileName, user, corpId);
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