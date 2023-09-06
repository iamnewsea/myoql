package nbcp.base.tool

import nbcp.base.TestBase
import nbcp.base.comm.JsonMap
import nbcp.base.comm.StringMap
import nbcp.base.comm.const
import nbcp.base.extend.*
import nbcp.base.utils.FileUtil
import nbcp.base.utils.HttpUtil
import org.junit.jupiter.api.Test
import org.springframework.scheduling.annotation.Scheduled
import java.io.File


class NpmPublishTest : TestBase() {
//    var nexusUrl = "http://172.10.185.1:8081/repository/npm-hosted";
    var nexusUrl = "http://dc-qq:8081/repository/npm2";

    @Test
    fun getDockerTags() {
        var f = File("D:\\code\\nancal\\lowcode-template-v2")
        var list = genPublish(f);


        var t = File(f, "npm_publish.sh");
        t.writeText(list.joinToString("\n"), const.utf8)
    }

    private fun genPublish(root: File): List<String> {
        var list = mutableListOf<String>()
        root.RecursionPaths {
            if (it.isFile) {
                return@RecursionPaths true;
            }

            var packageJson = it.listFiles().firstOrNull { it.name == "package.json" }
            if (packageJson == null) {
                return@RecursionPaths true;
            }

            list.addAll(getScript(it, root, packageJson))
            list.add("")

            return@RecursionPaths true;
        }
        return list;
    }

    private fun getScript(file: File, root: File, packageJson: File): List<String> {
        var list = mutableListOf<String>()
        var path = file.getRelativePath(root);
        if (path.isEmpty()) {
            return list;
        }
        path = path.replace('\\','/');
        println(file.path)
        var packContent = try {
            packageJson.readText(const.utf8).FromJson<JsonMap>()!!;
        } catch (e: Exception) {
            println("package.json 转换Json出错!")
            println(packageJson.readText(const.utf8))
            return list;
        }
        var name = packContent.get("name").AsString() + "@" + packContent.get("version").AsString();

        list.add("echo 安装${name}, 路径: ${path}");
        list.add("cd ${path}");
        list.add("npm publish --registry ${nexusUrl}");
        list.add("cd -");
        return list;
    }
}