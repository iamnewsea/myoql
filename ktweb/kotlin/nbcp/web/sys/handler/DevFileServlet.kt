package nbcp.web.sys.handler

import nbcp.base.annotation.Require
import nbcp.base.comm.FileExtensionInfo
import nbcp.base.comm.JsonResult
import nbcp.base.comm.ListResult
import nbcp.base.extend.AsString
import nbcp.base.extend.FullName
import nbcp.base.extend.HasValue
import nbcp.base.utils.ShellUtil
import nbcp.base.utils.WebUtil
import nbcp.mvc.sys.setDownloadFileName
import nbcp.mvc.annotation.*
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest
import java.io.File
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * 客户端启动，接收浏览器请求，操作本地文件。
 */
@RestController
@AdminSysOpsAction
@RequestMapping("/dev/file")
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
class DevFileServlet {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    /**
     * 通过Http上传的方式保存文件
     */
    @PostMapping("/save")
    fun save(@Require work_path: String, request: HttpServletRequest): JsonResult {
        var target = File(work_path);
        if (target.exists()) {
            if (target.isFile == false) {
                return JsonResult.error("path已存在且是目录");
            }
        } else {
            if (target.parentFile.exists() == false) {
                target.parentFile.mkdirs();
            }
        }

        if (request is StandardMultipartHttpServletRequest == false) {
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

    /**
     * 下载 文件内容
     */
    @GetMapping("/download")
    fun download(@Require work_path: String, @Require name: String, response: HttpServletResponse) {
        var target = work_path + File.separator + name;
        var targetFile = File(target);
        if (targetFile.exists() == false) {
            throw RuntimeException("文件 ${targetFile.FullName} 不存在")
        }
        response.setDownloadFileName(name)
        response.outputStream.write(targetFile.readBytes())
    }

    /**
     * 查看 文件内容
     */
    @GetMapping("/view")
    fun view(@Require work_path: String, @Require name: String, response: HttpServletResponse) {
        var target = work_path + File.separator + name;
        var targetFile = File(target);
        if (targetFile.exists() == false) {
            throw RuntimeException("文件 ${targetFile.FullName} 不存在")
        }

        var fileInfo = FileExtensionInfo.ofFileName(name);
        response.contentType = WebUtil.getMimeType(fileInfo.extName).AsString("text/plain")

        response.outputStream.write(targetFile.readBytes())
    }


    @PostMapping("/list")
    fun list(@Require work_path: String): ListResult<String> {
        var docker_cmd = "ls -ahl  ${work_path}"
        return execCmd(docker_cmd);
    }

    @PostMapping("/delete")
    fun delete(@Require work_path: String, @Require name: String): ListResult<String> {
        var docker_cmd = "rm -rf  ${work_path}"
        return execCmd(docker_cmd);
    }

    @PostMapping("/mkdir")
    fun mkdir(@Require work_path: String, @Require name: String): JsonResult {
        var docker_cmd = "mkdir -p  ${work_path}"
        return execCmd(docker_cmd);
    }

    @PostMapping("/rename")
    fun rename(@Require work_path: String, @Require name: String, @Require newName: String): JsonResult {
        var docker_cmd = "mv ${work_path}/${name} ${work_path}/${newName}"
        return execCmd(docker_cmd)
    }


    fun execCmd(vararg cmds: String): ListResult<String> {
        ShellUtil.execRuntimeCommand(cmds.toList())
            .apply {
                if (this.msg.HasValue) {
                    return ListResult.error(this.msg)
                }

                return ListResult.of(this.data!!.split("\n"))
            }
    }
}