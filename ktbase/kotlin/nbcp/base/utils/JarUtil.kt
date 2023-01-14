package nbcp.base.utils

import nbcp.base.comm.MvnDependencyBasicData
import nbcp.base.extend.AsLocalDateTime
import nbcp.base.extend.FullName
import nbcp.base.extend.Slice
import nbcp.base.extend.readContentString
import org.reflections.util.ClasspathHelper
import java.io.File
import java.net.URL
import java.time.LocalDateTime
import java.util.*
import java.util.jar.JarFile

object JarUtil {

    val startJarPackage: String by lazy {
        return@lazy Thread.currentThread().stackTrace.last().className.split(".").Slice(0, -1).joinToString(".")
    }


    fun getJarExePath(): String {
        val osName = System.getProperty("os.name").lowercase(Locale.getDefault())
        var javaHomePath = System.getProperty("java.home")

        //修复！
        if (javaHomePath.endsWith("jre")) {
            javaHomePath = FileUtil.joinPath(javaHomePath, "../")
        }
        var jarExePath = FileUtil.joinPath(javaHomePath, "bin", "jar")
        if (osName.contains("windows")) {
            jarExePath += ".exe"
        }
        if (File(jarExePath).exists() == false) {
            throw RuntimeException("找不到 jar 命令：$jarExePath")
        }
        return jarExePath
    }


    @JvmStatic
    fun getMainApplicationLastModified(): LocalDateTime? {
        val list = ClasspathHelper.forResource("");
        if (list.any() == false) return null;

        val fileName = list.first().path;

        return Date(File(fileName).lastModified()).AsLocalDateTime()
    }


    /**
     * 判断是否是 Jar包启动。
     */
    @JvmStatic
    fun isJarStarting(): Boolean {
        return Thread.currentThread().contextClassLoader.getResource("/") != null;
    }

    @JvmStatic
    fun getStartingJarFile(url: URL): File? {
        val path = UrlUtil.decodeURIComponent(url.path)
        if (url.protocol == "jar") {
            //值是： file:/D:/code/sites/server/admin/target/admin-api-1.0.1.jar!/BOOT-INF/classes!/
            var index = 0;
            if (path.startsWith("file:/")) {
                index = "file:/".length;
            }
            return File(path.Slice(index - 1, -"!/BOOT-INF/classes!/".length))
        } else if (url.protocol == "file") {
            //值是： /D:/code/sites/server/admin/target/classes/
            //处理文件路径中中文的问题。
            val targetPath = File(path).parentFile
            val mvn_file = targetPath?.listFiles { it -> it.name == "maven-archiver" }?.firstOrNull()
                    ?.listFiles { it -> it.name == "pom.properties" }?.firstOrNull()
            if (mvn_file == null) {
                return null;
//                throw RuntimeException("找不到 maven-archiver , 先打包再运行!")
            }

            val jarFile_lines = mvn_file.readLines()
            val version = jarFile_lines.first { it.startsWith("version=") }.split("=").last()
            val artifactId = jarFile_lines.first { it.startsWith("artifactId=") }.split("=").last()

            return File(targetPath.FullName + "/" + artifactId + "-" + version + ".jar")
        }

        return null;
//        throw RuntimeException("不识别的协议类型 ${url.protocol}")
    }

    /**
     * 获取启动Jar所的路径
     * 调试时，会返回 target/classes/nbcp/base/utils
     */
    @JvmStatic
    fun getStartingJarFile(): File? {
//        val stackTraceElements = RuntimeException().stackTrace
//        for (stackTraceElement in stackTraceElements) {
//            if ("main" == stackTraceElement.methodName) {
//                return stackTraceElement.className
//            }
//        }
        /**
        file:/opt/edu_report/admin-api-1.0.1.jar!/BOOT-INF/classes!/
        /D:/code/edu_report/server/admin/target/classes/
         */
        /**
         * 还有一种办法获取 file
         * var file = Thread.currentThread().contextClassLoader.getResource("./").path
         */
//        var file = clazz.protectionDomain.codeSource.location.path
        val classLoader = Thread.currentThread().contextClassLoader

        /**
         * jar -Dloader.path=libs 方式:
         * 1. 使用 /, 表示启动的Jar包 !/BOOT-INF/classes!/
         * 2. 使用 ./ 或 空串 表示 libs 目录
         *
         * jar 方式：
         * 1. 使用 /, 表示启动的Jar包 !/BOOT-INF/classes!/
         * 2. 使用 ./ 或 空串 表示Jar包
         *
         * 调试时：
         * 1. 使用 / 返回 null
         * 2. 使用 ./ 或 空串 ,返回 /D:/code/sites/server/admin/target/classes/
         */
        val url = classLoader.getResource("./") ?: classLoader.getResource("")
        return getStartingJarFile(url)
    }


    /**
     * 从外部Jar包获取元数据信息
     */
    fun getJarMetaInfo(file: File): MvnDependencyBasicData {
        return JarFile(file)
                .use { jarFile ->
                    val entries = jarFile.entries()
                    while (entries.hasMoreElements()) {
                        val jarEntry = entries.nextElement()
                        if (jarEntry.isDirectory) {
                            continue
                        }
                        if (jarEntry.name.startsWith("META-INF/maven")
                                && jarEntry.name.endsWith("/pom.properties")) {


                            return@use getGroupIdFromPomProperties(jarFile.getInputStream(jarEntry).readContentString())

                        }
                    }
                    return@use MvnDependencyBasicData();
                }
    }

    private fun getGroupIdFromPomProperties(content: String): MvnDependencyBasicData {
        var ret = MvnDependencyBasicData();
        val lines = content.split("\n".toRegex()).toTypedArray()
        for (line in lines) {
            var kv = line.split("=");
            if (kv.size == 1) {
                content;
            }

            if (kv[0] == "groupId") {
                ret.groupId = kv[1].trim();
            } else if (kv[0] == "artifactId") {
                ret.artifactId = kv[1].trim();
            } else if (kv[0] == "version") {
                ret.version = kv[1].trim();
            }
        }
        return ret
    }
}