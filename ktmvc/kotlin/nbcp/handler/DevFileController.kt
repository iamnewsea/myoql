package nbcp.handler

import nbcp.comm.*
import nbcp.utils.*
import nbcp.db.IdUrl
import nbcp.web.setDownloadFileName
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.lang.RuntimeException
import java.time.LocalDate
import java.time.LocalTime
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * 客户端启动，接收浏览器请求，写入本地文件
 */
@RestController
@ConditionalOnProperty("server.dev")
@RequestMapping("/dev/file")
class DevFileController {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    /**
     * 获取 docker容器
     */
    @PostMapping("/save")
    fun save(@Require path: String, request: HttpServletRequest): JsonResult {
        var target = File(path);
        if (target.exists()) {
            if (target.isFile == false) {
                return JsonResult("path已存在且是目录", "path");
            }
        } else {
            if (target.parentFile.exists() == false) {
                target.parentFile.mkdirs();
            }
        }

        if (request is StandardMultipartHttpServletRequest) {
            throw RuntimeException("request非StandardMultipartHttpServletRequest类型")
        }

        var list = (request as StandardMultipartHttpServletRequest).multiFileMap.toList();
        if (list.size != 0) {
            throw RuntimeException("找不到文件")
        } else if (list.size != 1) {
            throw RuntimeException("一次只能上传一个文件")
        }

        var files = list.first().second;
        if (files.size != 0) {
            throw RuntimeException("找不到文件")
        } else if (files.size != 1) {
            throw RuntimeException("一次只能上传一个文件")
        }

        var file = files.first()
        file.transferTo(target)
        return JsonResult();
    }
}