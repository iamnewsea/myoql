package nbcp.web.comm

import nbcp.base.extend.*
import nbcp.base.utils.ClassUtil
import nbcp.base.utils.FileUtil
import nbcp.base.utils.JarUtil
import nbcp.base.utils.SpringUtil
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*

class WebAppInfo {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass);
        fun getAppInfo(): String {


            var style = """<style>${ClassUtil.getDefaultClassLoader().getResourceAsStream("hi.css").readContentString()}</style>"""

            return style +
                    "<h1>" + SpringUtil.context.environment.getProperty("spring.application.name") +
                    "</h1><div class='grid'>" +
                    getBasicInfo()
                            .filter { it.value.HasValue }
                            .map { "<div><span>${it.key}</span><span>${it.value}</span></div>" }
                            .joinToString("") +
                    "</div><br /> <h2>构建信息 (.ops.report.txt)</h2><div class='grid'>" +
                    getDeployReport()
                            .filter { it.value.HasValue }
                            .map { "<div><span>${it.key}</span><span>${it.value}</span></div>" }
                            .joinToString("") +
                    "</div>"
        }

        fun getBasicInfo(): Map<String, String?> {
            val json = mutableMapOf<String, String?>();
            val env = SpringUtil.context.environment;

            val jarFile = JarUtil.getStartingJarFile();
            json["当前配置"] = env.activeProfiles.joinToString(",")
            json["集群"] = env.getProperty("app.group");

            if (jarFile != null) {
                json["启动文件"] = jarFile.name;
                json["启动文件时间"] = Date(jarFile.lastModified()).AsString();
            }
            json["JAVA_VERSION"] = System.getProperty("java.version");
            json["JAVA_OPTS"] = System.getenv("JAVA_OPTS");
            json["HOST名称"] = System.getenv("HOSTNAME");


            return json
        }


        fun getDeployReport(): Map<String, String?> {
            val jarFile = JarUtil.getStartingJarFile();
            if (jarFile == null) {
                return mapOf()
            }

            var reportFile = File(FileUtil.resolvePath(jarFile.parentFile.FullName, ".ops.report.txt"))


            if (!reportFile.exists()) {
                logger.Important("找不到构建信息文件: " + reportFile.FullName)
                return mapOf()
            }

            return reportFile.readText(Charsets.UTF_8).split("\n")
                    .map { it.trim() }
                    .filter { it.contains(":") }
                    .map {
                        var index = it.indexOf(":")
                        return@map it.substring(0, index) to it.substring(index + 1)
                    }.toMap()
        }
    }
}