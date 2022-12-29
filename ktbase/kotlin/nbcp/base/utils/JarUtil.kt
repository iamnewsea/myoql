package nbcp.base.utils

import nbcp.base.comm.MvnDependencyBasicData
import nbcp.base.extend.readContentString
import java.io.File
import java.util.jar.JarFile

object JarUtil {

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