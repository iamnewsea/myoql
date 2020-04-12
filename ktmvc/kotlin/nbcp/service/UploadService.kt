package nbcp.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.multipart.MultipartHttpServletRequest
import org.springframework.web.multipart.commons.CommonsMultipartResolver
import nbcp.comm.*
import nbcp.utils.*
import nbcp.db.*
import nbcp.db.mongo.*
import nbcp.db.mongo.entity.*
import nbcp.db.mongo.service.UploadFileMongoService
import nbcp.db.mongo.table.MongoBaseGroup
import nbcp.db.mysql.service.UploadFileMysqlService
import nbcp.model.IUploadFileDbService
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty

import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.lang.RuntimeException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.imageio.ImageIO
import javax.servlet.http.HttpServletRequest
import nbcp.db.mongo.entity.*

/**
 * 参数传递过程中,都没有 uploadPath 部分.
 */
@Service
@ConditionalOnProperty("app.upload.path")
open class UploadService {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    data class FileNameData(var msg: String = "") {
        var oriName: String = ""
        var extName: String = ""
        var extType: FileExtentionTypeEnum = FileExtentionTypeEnum.Other
        var imgWidth: Int = 0
        var imgHeight: Int = 0

        /*
     * 把文件转移到相应的文件夹下.
     * 1. 第一级目录,按 年-月 归档.
     * 2. 第二级目录,按 企业Id 归档.
     *      2.1.如果是后台, 企业Id = _admin_
     *      2.2.如果是商城用户,企业Id = _shop_
     * 3. 第三级目录,如果是非图片是 extType , 如果是图片是 宽-高
     * 4. 第四级是,如果是图片,是256宽度像素大小的缩略图.
         */
        fun getTargetPaths(): Array<String> {
            var list = mutableListOf<String>()
            list.add(LocalDate.now().format("yyyy-MM"));

            if (this.extType == FileExtentionTypeEnum.Image) {
                list.add("${imgWidth}-${imgHeight}")
            } else {
                list.add(this.extType.toString())
            }

            return list.toTypedArray();
        }

        fun getTargetFileNames(): Array<String> {
            return arrayOf(*this.getTargetPaths(), CodeUtil.getCode() + "." + extName);
        }

        fun getTargetFileName(corpId: String): String {
            var targetFileName = mutableListOf<String>()
            if (corpId.HasValue) {
                targetFileName.add(corpId);
            }
            targetFileName.addAll(getTargetFileNames())
            return targetFileName.map { File.separator + it }.joinToString("")
        }
    }


    @Value("\${app.upload.path:}")
    private var uploadPath = ""

    @Value("\${app.upload.saveCorp:true}")
    private var saveCorp = false

    @Value("\${app.upload.dbType:Mongo}")
    private var dbType = "Mongo"

//小图，在上传时不生成。 在请求时在内存中压缩即时生成。
//    @Value("\${server.upload.logoSize:0}")
//    private var logoSize = 0

    /**
     * checkCode 生成方式，md5,mymd5,两种
     */
    @Value("\${app.upload.checkType:md5}")
    private var checkType = "md5"

    fun downloadImage(url: String, corp: IdName, user: IdName, maxWidth: Int = 1200): ApiResult<IdUrl> {
        var fileMsg = HttpUtil.getImage(url, uploadPath + File.separator + "_temp_", maxWidth);
        if (fileMsg.msg.HasValue) {
            return ApiResult<IdUrl>(msg = fileMsg.msg);
        }


        var vFile = File.separator + "_temp_" + File.separator + fileMsg.name + "." + fileMsg.extName;
        return doUpload(vFile, user, corp.id)
    }


    private fun getFileInfo(fileName: String): FileExtentionInfo {

        var fileName = fileName.Remove("/", "\\", "?", "#", "\"", "'", " ", "%", "&", ":", "@", "<", ">")
        var extInfo = FileExtentionInfo(fileName);

        if (fileName.length < 4) {
            extInfo.name = "";
        }
        return extInfo;
    }

    fun getFileInfo(request: HttpServletRequest): FileExtentionInfo {
        var oriFileName = request.getHeader("File-Name");
        oriFileName = oriFileName.Remove("/", "\\", "?", "#", "\"", "'", " ", "%", "&", ":", "@", "<", ">")
        var extInfo = FileExtentionInfo(oriFileName);
        if (oriFileName.length < 4) {
            extInfo.name = "";
        }
        return extInfo;
    }

    /**
     * @return 返回相对于 uploadPath 的路径.
     */
    private fun saveTempFile(file: MultipartFile, extName: String): String {
        var ret = ApiResult<String>();

        if (file.size == 0L) {
            throw  Exception("上传的是空文件！");
        }

        var vFile = File.separatorChar + "_temp_" + File.separator + CodeUtil.getCode() + "." + extName;
        var localFile = File(uploadPath + File.separator + vFile)

        if (localFile.parentFile.exists() == false && localFile.parentFile.mkdirs() == false) {
            throw Exception("创建文件夹失败:${localFile.parent}");
        }

        file.transferTo(localFile)
        return vFile;
    }


    /**
     * @return 返回相对于 uploadPath 的路径.
     */
    private fun saveTempFile(file: InputStream, extName: String): String {

        var vFile = File.separatorChar + "_temp_" + File.separator + CodeUtil.getCode() + "." + extName;
        var localFile = File(uploadPath + vFile)

        file.copyTo(localFile.outputStream())
        return vFile
    }


    /**
     * @param vTempFile , 相对于 uploadPath 的相对路径.
     */
    private fun doUpload(vTempFile: String, user: IdName, corpId: String, oriFileName: String = ""): ApiResult<IdUrl> {

        var vFile = File(uploadPath + vTempFile);
        if (vFile.exists() == false) {
            return ApiResult<IdUrl>("找不到文件:${vFile.FullName}")
        }

        var oriMd5 = getMd5(vFile)
        if (oriMd5.isEmpty()) {
            return ApiResult<IdUrl>("计算 Md5 出错:${vFile.FullName}")
        }

        //仅在当前企业下判断,如果重复,则不记录到数据库.
        var md5Annex = dbService.getByMd5(oriMd5);

        if (md5Annex != null) {
            return ApiResult.of(IdUrl(md5Annex.id, md5Annex.url))
        }

        var extInfo = FileExtentionInfo(vTempFile);

        var fileData = FileNameData();
        fileData.oriName = oriFileName
        fileData.extName = extInfo.extName
        fileData.extType = extInfo.extType


        var annexInfo = SysAnnex();
        annexInfo.ext = extInfo.extName
        annexInfo.size = vFile.length().AsInt()
        annexInfo.checkCode = oriMd5
        annexInfo.creator = user


        if (extInfo.extType == FileExtentionTypeEnum.Image) {
            val bufferedImage = ImageIO.read(vFile)

            annexInfo.imgWidth = bufferedImage.getWidth();
            annexInfo.imgHeight = bufferedImage.getHeight();

            fileData.imgWidth = annexInfo.imgWidth;
            fileData.imgHeight = annexInfo.imgHeight;
        }

        var targetFileName = fileData.getTargetFileName(if (saveCorp) corpId else "")


        annexInfo.url = renameFile(vTempFile, targetFileName)

//        if (fileData.extType == FileExtentionTypeEnum.Image && logoSize > 0) {
//            ImageUtil.zoomImageScale(uploadPath + annexInfo.url, uploadPath + annexInfo.url + ".${logoSize}." + fileData.extName, logoSize, logoSize)
//        }

        annexInfo.url = annexInfo.url.replace("\\", "/");

        if (saveCorp) {
            annexInfo.corpId = corpId
        }

        if (dbService.insert(annexInfo) == 0) {
            return ApiResult<IdUrl>("记录到数据出错")
        }
        return ApiResult.of(IdUrl(annexInfo.id, annexInfo.url))
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
    private fun renameFile(tempPath: String, targetPath: String): String {
        if (tempPath == targetPath) {
            return targetPath;
        }

        var localFile = File(uploadPath + tempPath);

        if (localFile.exists() == false) {
            throw Exception("找不到保存的文件")
        }
//        var targetFileSects = fileData.getTargetFileName() targetFileSects.joinToString(File.separator)

        var targetFile = File(uploadPath + targetPath)
        if (targetFile.parentFile.exists() == false && targetFile.parentFile.mkdirs() == false) {
            return "创建文件夹失败： ${targetFile.parentFile.FullName}"
        }

        if (localFile.renameTo(targetFile) == false) {
            return "文件重命令名错误: ${localFile.FullName} ,${targetFile.FullName}";
        }


        return targetPath
    }


    private fun getMd5(localFile: File): String {
        var fileStream = FileInputStream(localFile);
        try {
            if (checkType VbSame "md5") {
                return Md5Util.getFileMD5(fileStream);
            }
            return Md5Util.getFileBase64MD5(fileStream);
        } finally {
            fileStream.close();
        }
    }

    private val dbService by lazy {
        if (this.dbType VbSame DatabaseEnum.Mongo.toString()) {
            return@lazy SpringUtil.context.getBean(UploadFileMongoService::class.java)
        } else {
            return@lazy SpringUtil.context.getBean(UploadFileMysqlService::class.java)
        }
    }


    /**
     * 按原始的Md5查询文件是否存在。
     */
    fun onFileMd5Check(md5: String, user: IdName, corpId: String): ApiResult<IdUrl> {
        if (md5.isEmpty()) {
            return ApiResult<IdUrl>();
        }

        if (saveCorp) {
            var annex = dbService.getByMd5(md5, corpId);

            if (annex != null) {
                if (File(uploadPath + annex.url).exists()) {
                    return ApiResult.of(IdUrl(id = annex.id, url = annex.url));
                } else {
                    dbService.clearMd5ById(annex.id);
                }
            }
        }

        var annex = dbService.getByMd5(md5)

        if (annex == null) {
            return ApiResult();
        }

        if (File(uploadPath + annex.url).exists() == false) {
            dbService.clearMd5ById(annex.id)
            return ApiResult();
        }


        if (saveCorp == false) {
            return ApiResult.of(IdUrl(annex.id, annex.url));
        }

        //copy file & copy record
        var fileData = FileNameData();
        fileData.oriName = annex.name
        fileData.extName = annex.ext
        fileData.extType = FileExtentionInfo(annex.url).extType
        fileData.imgWidth = annex.imgWidth
        fileData.imgHeight = annex.imgHeight


        var targetFileName = fileData.getTargetFileName(if (saveCorp) corpId else "")


        var r = File(uploadPath + annex.url).copyTo(File(uploadPath + targetFileName))
        if (r.exists() == false) {
            return ApiResult()
        }

        annex.url = targetFileName;
        annex.id = "";
        annex.corpId = corpId;
        annex.createAt = LocalDateTime.now()


        if (dbService.insert(annex) == 0) {
            return ApiResult<IdUrl>()
        }

        return ApiResult.of(IdUrl(annex.id, annex.url));
    }


    /**
     * 文件上传
     */
    fun upload(request: HttpServletRequest, user: IdName, corpId: String, processFile: ((String, FileExtentionTypeEnum) -> Unit)? = null): ListResult<IdUrl> {

        var ret = ListResult<IdUrl>();
        var list = mutableListOf<IdUrl>()
        if (request is StandardMultipartHttpServletRequest) {
            request.multiFileMap.toList().ForEachExt { it, _ ->
                var fileName = it.first;
                var files = it.second;

                files.ForEachExt for2@{ file, _ ->
                    var oriFileExtentionInfo = getFileInfo(file.originalFilename.AsString(fileName))
                    var vTempFile = saveTempFile(file, oriFileExtentionInfo.extName);
                    if (processFile != null) {
                        processFile(uploadPath + vTempFile, oriFileExtentionInfo.extType)
                    }

                    var ret1 = doUpload(vTempFile, user, corpId, oriFileExtentionInfo.name + "." + oriFileExtentionInfo.extName);
                    if (ret1.msg.HasValue) {
                        ret.msg = ret1.msg;
                        return@ForEachExt false
                    }
                    list.add(ret1.data!!);
                    return@for2 true
                }

                return@ForEachExt true;
            }
        }

        ret.data = list
        return ret;
    }
}