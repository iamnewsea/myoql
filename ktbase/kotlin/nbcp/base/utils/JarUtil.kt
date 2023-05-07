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
            javaHomePath = FileUtil.resolvePath(javaHomePath, "../")
        }
        var jarExePath = FileUtil.resolvePath(javaHomePath, "bin", "jar")
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
     * 获取启动Jar所的路径
     * 调试时，会返回 target/classes/nbcp/base/utils
     */
    @JvmStatic
    fun getStartingJarFile(): File {
        var targetPath = System.getProperty("java.class.path").split(";").filter{ it.startsWith(System.getProperty("user.dir"))}.firstOrNull() ?: "";
        return File(FileUtil.resolvePath(System.getProperty("user.dir"), targetPath))
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