package nbcp.service

import nbcp.comm.*
import nbcp.db.DatabaseEnum
import nbcp.db.IdName
import nbcp.db.IdUrl
import nbcp.db.db
import nbcp.db.mongo.entity.SysAnnex
import nbcp.db.mongo.service.UploadFileMongoService
import nbcp.db.mysql.service.UploadFileMysqlService
import nbcp.utils.CodeUtil
import nbcp.utils.HttpUtil
import nbcp.utils.Md5Util
import nbcp.utils.SpringUtil
import nbcp.web.WebUserTokenBean
import org.bytedeco.javacv.FFmpegFrameGrabber
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import javax.imageio.ImageIO
import javax.servlet.http.HttpServletRequest


/**
 * 参数传递过程中,都没有 uploadPath 部分.
 */

interface SaveFileForUploadService {
    /**
     * @param tempPath:源文件临时路径
     * @param fileData:源文件信息，文件名，图片大小，视频时长
     * @param saveCorp:是否按企业Id分别保存
     * @param corpId: 企业Id
     * @return 可下载的地址。 （如果保存在本地，可以是不带host头的地址，但必须配置到 nginx中）
     */
    fun save(tempPath: String, fileData: UploadService.FileNameData, saveCorp: Boolean, corpId: String):String
}


@Component
class SaveFileForUploadServiceBeanInstance : BeanPostProcessor {
    companion object {
        //如果没有配置WebUserTokenBean,那么使用session
        var instances: MutableList<SaveFileForUploadService> = mutableListOf()
    }

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any? {
        if (bean is SaveFileForUploadService) {
            instances.add(bean)
        }

        return super.postProcessAfterInitialization(bean, beanName)
    }
}