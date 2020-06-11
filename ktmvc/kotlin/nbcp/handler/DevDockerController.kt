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
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.time.LocalDate
import java.time.LocalTime
import javax.servlet.http.HttpServletResponse

/**
 * 三个固定参数： token,container,work_path,bash是可选
 */
@RestController
@ConditionalOnProperty("server.dev")
@RequestMapping("/dev/docker")
class DevDockerController {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    /**
     * 获取 docker容器
     */
    @PostMapping("/containers")
    fun getContainers(name: String): ListResult<String> {
        if (name.HasValue) {
            return ListResult.of(listOf(name))
        }

        var ret = execCmd("docker", "ps", "-a", "--format", "table {{.Names}}")
        if (ret.msg.HasValue == false) {
            ret.data = ret.data.Skip(1);
        }
        return ret;
    }


    /**
     * 列出内容
     */
    @PostMapping("/list")
    fun list(@Require container: String, bash: String, @Require work_path: String): ListResult<String> {
        var docker_cmd = "ls -ahl  ${work_path}"
        return execCmd("docker", "exec", container, bash.AsString("bash"), "-c", docker_cmd);
    }

    @Value("\${app.upload.path}")
    lateinit var uploadPath: String

    val path: String
        get() = uploadPath + File.separator + "docker-" + LocalDate.now().format("YYYY-MM-dd") + File.separator

    /**
     * 把文件拷到宿主机
     */
    @PostMapping("/copy2host")
    fun copy2host(@Require container: String, @Require work_path: String, @Require name: String): JsonResult {
        var targetPathName = path + LocalTime.now().format("HHmmss") + File.separator;
        var targetPath = File(targetPathName);

        if (targetPath.exists() == false && targetPath.mkdirs() == false) {
            throw Exception("创建文件夹失败:${targetPath.FullName}");
        }

        var target = targetPathName + "/" + name;
        execCmd("docker", "cp", "${container}:${work_path}/${name}", target);

        return JsonResult();
    }

    /**
     * 查看 文件内容
     */
    @GetMapping("/file")
    fun file(@Require container: String, @Require work_path: String, @Require name: String, view: Boolean?, response: HttpServletResponse) {
        var targetPathName = path;
        var targetPath = File(targetPathName);

        if (targetPath.exists() == false && targetPath.mkdirs() == false) {
            throw Exception("创建文件夹失败:${targetPath.FullName}");
        }

        var target = targetPathName + "/" + CodeUtil.getCode() + "-" + name;
        execCmd("docker", "cp", "${container}:${work_path}/${name}", target);

        var view = view ?: false;
        if (view) {
            var fileInfo = FileExtentionInfo(name);
            response.contentType = MyUtil.getMimeType(fileInfo.extName).AsString("text/plain")
        } else {
            response.setDownloadFileName(name)
        }
        response.outputStream.write(File(target).readBytes())
    }


    /**
     * 上传
     */
    @PostMapping("/upload")
    fun upload(@Require container: String, @Require work_path: String, @Require name: String, @Require dbFile: IdUrl): JsonResult {
        execCmd("docker", "cp", "${path}${dbFile.url}", "${container}:${work_path}/${name}");
        return JsonResult()
    }

    /**
     * 改名
     */
    @PostMapping("/rename")
    fun rename(@Require container: String, bash: String, @Require work_path: String, @Require name: String, @Require newName: String): JsonResult {
        var docker_cmd = "mv ${work_path}/${name} ${work_path}/${newName}"
        execCmd("docker", "exec", container, bash.AsString("bash"), "-c", docker_cmd);
        return JsonResult()
    }


    /**
     * 创建文件夹
     */
    @PostMapping("/mkdir")
    fun mkdir(@Require container: String, bash: String, @Require work_path: String, @Require name: String): JsonResult {
        var docker_cmd = "mkdir -p  ${work_path}/${name}"
        execCmd("docker", "exec", container, bash.AsString("bash"), "-c", docker_cmd);
        return JsonResult()
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    fun delete(@Require container: String, bash: String, @Require work_path: String, @Require name: String): JsonResult {
        var docker_cmd = "rm -rf  ${work_path}/${name}"
        execCmd("docker", "exec", container, bash.AsString("bash"), "-c", docker_cmd);
        return JsonResult()
    }

    fun execCmd(vararg cmds: String): ListResult<String> {
        logger.warn(cmds.joinToString(" "));
        var p = Runtime.getRuntime().exec(cmds);
        var sb = StringBuilder();
        var lines = listOf<String>()

        var br: BufferedReader? = null;
        try {
            p.waitFor()
            br = BufferedReader(InputStreamReader(p.getInputStream(), "utf-8"));
            lines = br.readLines()
        } catch (e: Exception) {
            return ListResult(e.message ?: "error")
        } finally {
            if (br != null) {
                try {
                    br.close();
                } finally {
                }
            }
        }
        return return ListResult.of(lines)
    }
}