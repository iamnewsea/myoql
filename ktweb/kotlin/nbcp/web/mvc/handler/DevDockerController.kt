package nbcp.web.mvc.handler

import nbcp.base.annotation.Require
import nbcp.base.comm.FileExtensionInfo
import nbcp.base.comm.ListResult
import nbcp.base.db.IdUrl
import nbcp.base.extend.AsString
import nbcp.base.extend.Format
import nbcp.base.extend.FullName
import nbcp.base.extend.HasValue
import nbcp.base.utils.CodeUtil
import nbcp.base.utils.MyUtil
import nbcp.base.utils.ShellUtil
import nbcp.mvc.mvc.setDownloadFileName
import nbcp.mvc.annotation.*
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.io.File
import java.time.LocalDate
import java.time.LocalTime
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

        var ret = execCmd("docker", "ps", "--format", "{{.Names}}")
        return ret;
    }


    /**
     * 列出内容
     */
    @PostMapping("/file/list")
    fun list(@Require container: String, bash: String, @Require work_path: String): ListResult<String> {
        return execDockerCmd(container, "ls -ahl  ${work_path}");
    }


    val path: String
        get() = MyUtil.joinFilePath(
            System.getProperty("java.io.tmpdir"),
            "docker-" + LocalDate.now().Format("yyyy-MM-dd")
        )

    /**
     * 把文件拷到宿主机
     */
    @PostMapping("/file/copy2host")
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
    @GetMapping("/file/download")
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
    @GetMapping("/file/view")
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
    @PostMapping("/file/upload")
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
    @PostMapping("/file/rename")
    fun rename(
        @Require container: String,
        bash: String,
        @Require work_path: String,
        @Require name: String,
        @Require newName: String
    ): ListResult<String> {
        return execDockerCmd(container, "mv ${work_path}/${name} ${work_path}/${newName}");
    }


    /**
     * 创建文件夹
     */
    @PostMapping("/file/mkdir")
    fun mkdir(
        @Require container: String,
        bash: String,
        @Require work_path: String,
        @Require name: String
    ): ListResult<String> {
        return execDockerCmd(container, "mkdir -p  ${work_path}/${name}")
    }

    /**
     * 删除
     */
    @PostMapping("/file/delete")
    fun delete(
        @Require container: String,
        bash: String,
        @Require work_path: String,
        @Require name: String
    ): ListResult<String> {
        return execDockerCmd(container, " rm -rf  ${work_path}/${name} ");
    }

    fun execDockerCmd(container: String, cmd: String): ListResult<String> {
        return execCmd("docker", "exec", container, "bash", "-c", cmd)
    }

    fun execCmd(vararg cmds: String): ListResult<String> {
        ShellUtil.execRuntimeCommand(cmds.toList())
            .apply {
                if (this.msg.HasValue) {
                    return ListResult.error(this.msg)
                }

                return ListResult.of(this.data!!.split("\n"))
            }
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