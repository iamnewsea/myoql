package nbcp.tool

import nbcp.base.comm.StringMap
import java.io.File
import java.lang.reflect.Field
import java.util.ArrayList

/**
 * 枚举Js的生成器
 */
object enumer {

    /**
     * 入口
     */
    fun work(basePackage: String) {
        println(ClassLoader.getSystemResource("").path)

        val fileList = getClassName(basePackage, "Enum")
        fileList.forEach {
            val jsonEnumClass = Class.forName(it);
            var jsonList = getEnums(jsonEnumClass);

            if (jsonList.size > 0) {
                println("""jv.defEnum("${jsonEnumClass.simpleName}",{""" +
                        jsonList.map { it.key + ":\"" + it.value + "\"" }.joinToString(",") + "});");
            }
        }
    }


    fun getClassName(packageName: String, extend: String): List<String> {
        val filePath = ClassLoader.getSystemResource("").path.replace("test-classes", "classes") + packageName.replace(".", File.separator)
        val fileNames = getClassName(filePath, extend, false)
        return fileNames
    }

    private fun getClassName(filePath: String, extend: String, classwithPath: Boolean): List<String> {
        val myClassName = ArrayList<String>()
        val file = File(filePath)
        val childFiles = file.listFiles()
        for (childFile in childFiles!!) {
            //获取子文件的名称
            val childFileName = childFile.name.replace(".class", "");
            //去除子文件名称的class
            if (childFile.isFile && childFileName.endsWith(extend)) {
                var childFilePath = childFile.path
                childFilePath = childFilePath.substring(childFilePath.indexOf(File.separator + "classes") + 9, childFilePath.lastIndexOf("."))
                childFilePath = childFilePath.replace(File.separator, ".")
                myClassName.add(childFilePath)
            }
        }

        return myClassName
    }

    private fun getEnums(jsonEnumClass: Class<*>): StringMap {
        var result = StringMap()
        if (jsonEnumClass.isEnum == false) return result;
        var fs = jsonEnumClass.declaredFields;
        var remark: Field
        try {
            remark = jsonEnumClass.getDeclaredField("remark");
        } catch (e: Exception) {
            return result;
        }

        remark.isAccessible = true;
        fs.all {
            if (it.name[0] == '$' && it.name.substring(1) == "VALUES") {
                return@all true;
            }
            if (it.type != jsonEnumClass) return@all true;
            var value = it.get(null);
            result.put(it.name, remark.get(value).toString());
            true;
        }
        return result;
    }
}
