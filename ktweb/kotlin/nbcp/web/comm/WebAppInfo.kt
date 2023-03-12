package nbcp.web.comm

import nbcp.base.extend.AsString
import nbcp.base.extend.FullName
import nbcp.base.extend.HasValue
import nbcp.base.utils.FileUtil
import nbcp.base.utils.JarUtil
import nbcp.base.utils.SpringUtil
import java.io.File
import java.util.*

class WebAppInfo {
    companion object {
        fun getAppInfo(): String {


            var style = """<style>
body{padding:16px;} 
div{margin-top:10px;} 
div>span:first-child{font-size:14px;color:gray} 
div>span:last-child{font-size:16px;} 
div>span:first-child::after{content:":";display:inline-block;margin-right:6px;}
h1{margin:0}
hr{height: 1px;border: none;border-top: 1px dashed gray;}
h2{margin:20px 0 0}
</style>"""

            return style +
                    "<h1>" + SpringUtil.context.environment.getProperty("spring.application.name") + "</h1><hr />" +
                    getBasicInfo()
                            .filter { it.value.HasValue }
                            .map { "<div><span>${it.key}</span><span>${it.value}</span></div>" }
                            .joinToString("") +
                    "<br /> <h2>构建信息</h2>" +
                    getDeployReport()
                            .filter { it.value.HasValue }
                            .map { "<div><span>${it.key}</span><span>${it.value}</span></div>" }
                            .joinToString("")
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

            var reportFile = File(FileUtil.resolvePath(jarFile.parentFile.FullName, "./.ops.report.txt"))


            if (!reportFile.exists()) {
                return mapOf()
            }

            return reportFile.readText(Charsets.UTF_8).split("\n")
                    .map { it.trim() }
                    .filter { !it.startsWith("﹍") && !it.startsWith("﹊") && it.contains(":") }
                    .map {
                        var index = it.indexOf(":")
                        return@map it.substring(0, index) to it.substring(index + 1)
                    }.toMap()
        }
    }
}