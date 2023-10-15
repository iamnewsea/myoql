package nbcp.base.tool

import nbcp.base.TestBase
import nbcp.base.comm.JsonMap
import nbcp.base.comm.const
import nbcp.base.enums.JsonStyleScopeEnum
import nbcp.base.extend.*
import nbcp.base.extend.*
import nbcp.base.extend.*
import nbcp.base.utils.FileUtil
import org.junit.jupiter.api.Test
import java.io.File


class NpmPublishTest : TestBase() {
    //    var nexusUrl = "http://172.10.185.1:8081/repository/npm-hosted";
    var nexusUrl = "http://dc-qq:8081/repository/npm2";

    @Test
    fun cp_package_json() {
        var source = File("D:\\code\\nancal\\v2")
        var target = File("D:\\code\\nancal\\v3")
        source.ListRecursionFiles({ true }, {
            if (it.name == "package.json") {
                var targetFile = FileUtil.resolvePath(target.path, it.getRelativePathStartsWith(source))
                it.copyTo(File(targetFile), true);
            }
            true;
        })
    }

    @Test
    fun getDockerTags() {
        var f = File("D:\\code\\nancal\\v2")
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

    private fun getScript(file: File, root: File, packageJsonFile: File): List<String> {
        var list = mutableListOf<String>()
        var path = file.getRelativePathStartsWith(root);
        if (path.isEmpty()) {
            return list;
        }
        path = path.replace('\\', '/');
        println(file.path)
        var packMap = try {
            packageJsonFile.readText(const.utf8).FromJson<JsonMap>()!!;
        } catch (e: Exception) {
            println("package.json 转换Json出错!")
            println(packageJsonFile.readText(const.utf8))
            return list;
        }

        if (packMap.get("name").AsString().isEmpty()) {
            return list;
        }

        packMap.remove("scripts");
        packMap.remove("publishConfig");

        packageJsonFile.writeText(packMap.ToJson(JsonStyleScopeEnum.PRETTY), const.utf8)

        var name = packMap.get("name").AsString() + "@" + packMap.get("version").AsString();

        list.add("echo 安装${name}, 路径: ${path}");
        list.add("cd ${path}");
        list.add("npm publish --registry ${nexusUrl}");
//        list.add("if [ $? -ne 0 ]; then echo 'error \${$?}'; exit 1; fi ");
        list.add("cd -");
        return list;
    }
}