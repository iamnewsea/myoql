package nbcp.base.mvc.handler

import nbcp.comm.*
import nbcp.utils.*
import nbcp.db.IdUrl
import nbcp.base.mvc.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.io.File
import java.time.LocalDate
import java.time.LocalTime
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


/**
 * 三个固定参数： token,container,work_path,bash是可选
 */
@RestController
@AdminSysOpsAction
@RequestMapping("/dev/docker")
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
class DevDockerServlet {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    /**
     * 获取 docker 容器
     */
    @PostMapping("/containers")
    fun getContainers(name: String): ListResult<String> {
        if (name.HasValue) {
            return ListResult.of(listOf(name))
        }

        var ret = execCmd("docker", "ps", "--format", "table {{.Names}}")
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

    @Value("\${app.upload.local.path:}")
    var uploadPath: String = ""

    val path: String
        get() = uploadPath + File.separator + "docker-" + LocalDate.now().Format("yyyy-MM-dd") + File.separator

    /**
     * 把文件拷到宿主机
     */
    @PostMapping("/copy2host")
    fun copy2host(@Require container: String, @Require work_path: String, @Require name: String): ListResult<String> {
        var targetPathName = path + LocalTime.now().Format("HHmmss") + File.separator;
        var targetPath = File(targetPathName);

        if (targetPath.exists() == false && targetPath.mkdirs() == false) {
            throw Exception("创建文件夹失败:${targetPath.FullName}");
        }

        var target = targetPathName + "/" + name;
        return execCmd("docker", "cp", "${container}:${work_path}/${name}", target);
    }

    /**
     * 下载 文件内容
     */
    @GetMapping("/download")
    fun download(
        @Require container: String,
        @Require work_path: String,
        @Require name: String,
        response: HttpServletResponse
    ) {
        var targetPathName = path;
        var targetPath = File(targetPathName);

        if (targetPath.exists() == false && targetPath.mkdirs() == false) {
            throw Exception("创建文件夹失败:${targetPath.FullName}");
        }

        var target = targetPathName + "/" + CodeUtil.getCode() + "-" + name;
        execCmd("docker", "cp", "${container}:${work_path}/${name}", target);


        response.setDownloadFileName(name)
        response.outputStream.write(File(target).readBytes())
    }

    /**
     * 查看 文件内容
     */
    @GetMapping("/view")
    fun view(
        @Require container: String,
        @Require work_path: String,
        @Require name: String,
        response: HttpServletResponse
    ) {
        var targetPathName = path;
        var targetPath = File(targetPathName);

        if (targetPath.exists() == false && targetPath.mkdirs() == false) {
            throw Exception("创建文件夹失败:${targetPath.FullName}");
        }

        var target = targetPathName + "/" + CodeUtil.getCode() + "-" + name;
        execCmd("docker", "cp", "${container}:${work_path}/${name}", target);


        var fileInfo = FileExtensionInfo.ofFileName(name);
        response.contentType = MyUtil.getMimeType(fileInfo.extName).AsString("text/plain")

        response.outputStream.write(File(target).readBytes())
    }


    /**
     * 上传
     */
    @PostMapping("/upload")
    fun upload(
        @Require container: String,
        @Require work_path: String,
        @Require name: String,
        @Require dbFile: IdUrl
    ): ListResult<String> {
        return execCmd("docker", "cp", "${path}${dbFile.url}", "${container}:${work_path}/${name}");
    }

    /**
     * 改名
     */
    @PostMapping("/rename")
    fun rename(
        @Require container: String,
        bash: String,
        @Require work_path: String,
        @Require name: String,
        @Require newName: String
    ): ListResult<String> {
        var docker_cmd = "mv ${work_path}/${name} ${work_path}/${newName}"
        return execCmd("docker", "exec", container, bash.AsString("bash"), "-c", docker_cmd)
    }


    /**
     * 创建文件夹
     */
    @PostMapping("/mkdir")
    fun mkdir(
        @Require container: String,
        bash: String,
        @Require work_path: String,
        @Require name: String
    ): ListResult<String> {
        var docker_cmd = "mkdir -p  ${work_path}/${name}"
        return execCmd("docker", "exec", container, bash.AsString("bash"), "-c", docker_cmd);
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    fun delete(
        @Require container: String,
        bash: String,
        @Require work_path: String,
        @Require name: String
    ): ListResult<String> {
        var docker_cmd = "rm -rf  ${work_path}/${name}"
        return execCmd("docker", "exec", container, bash.AsString("bash"), "-c", docker_cmd);
    }

    fun execCmd(vararg cmds: String): ListResult<String> {
        return ListResult.of(ShellUtil.execRuntimeCommand(*cmds))
//        logger.warn(cmds.joinToString(" "));
//        var p = Runtime.getRuntime().exec(cmds);
//        var lines = listOf<String>()
//
//        var br: BufferedReader? = null;
//        try {
//            p.waitFor()
//            if (p.exitValue() == 0) {
//                br = BufferedReader(InputStreamReader(p.inputStream, "utf-8"));
//                lines = br.readLines()
//                return ListResult.of(lines)
//            } else {
//                br = BufferedReader(InputStreamReader(p.errorStream, "utf-8"));
//                lines = br.readLines();
//                return ListResult(lines.joinToString(","))
//            }
//        } catch (e: Exception) {
//            return ListResult(e.message ?: "error")
//        } finally {
//            if (br != null) {
//                try {
//                    br.close();
//                } finally {
//                }
//            }
//        }
    }
}