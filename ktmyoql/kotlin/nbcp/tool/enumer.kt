package nbcp.tool

import nbcp.comm.*
import nbcp.utils.ClassUtil
import nbcp.utils.MyUtil
import java.io.File
import java.lang.RuntimeException
import java.lang.reflect.Field
import java.util.ArrayList

/**
 * 枚举Js的生成器
 */
object enumer {

    /**
     * 入口
     */
    fun work(basePackage: String): List<String> {
        var ret = mutableListOf<String>()
        val fileList = ClassUtil.getClasses(basePackage, Enum::class.java)

        fileList.forEach {
            ret.add(work(it))
        }
        return ret;
    }

    fun work(jsonEnumClass: Class<*>): String {
        if (jsonEnumClass.isEnum == false) {
            return "";
        }
        var jsonList = jsonEnumClass.GetEnumList()

        var ret = "";
        if (jsonList.size > 0) {
            var strField = jsonEnumClass.GetEnumStringField();

            ret = """jv.defEnum("${jsonEnumClass.simpleName}",{""" +
                    jsonList.map {
                        it.toString() + ":\"" +
                                (if (strField != null) MyUtil.getPrivatePropertyValue(
                                    it,
                                    strField
                                ) else it.toString()) + "\""
                    }.joinToString(",") + "});"

            println(ret);
        }

        return ret;
    }

//
//    fun getClassName(packageName: String, callback: ((String) -> Boolean)? = null): List<String> {
//        val filePath = ClassLoader.getSystemResource("").path.replace("test-classes", "classes") + packageName.replace(
//            ".",
//            File.separator
//        )
//        val fileNames = getClassNameFromPath(filePath, callback)
//        return fileNames
//    }
//
//    private fun getClassNameFromPath(filePath: String, callback: ((String) -> Boolean)? = null): List<String> {
//        val myClassName = ArrayList<String>()
//        val file = File(filePath)
//        if (file.exists() == false) {
//            throw RuntimeException("找不到路径: ${file.FullName}")
//        }
//        val childFiles = file.listFiles()
//        for (childFile in childFiles!!) {
//            //获取子文件的名称
//            val childFileName = childFile.name.replace(".class", "");
//            //去除子文件名称的class
//            if (childFile.isFile && (callback == null || callback(childFileName))) {
//                var childFilePath = childFile.path
//                childFilePath = childFilePath.substring(
//                    childFilePath.indexOf(File.separator + "classes") + 9,
//                    childFilePath.lastIndexOf(".")
//                )
//                childFilePath = childFilePath.replace(File.separator, ".")
//                myClassName.add(childFilePath)
//            }
//        }
//
//        return myClassName
//    }
//
//    private fun getEnums(jsonEnumClass: Class<*>): StringMap {
//        var result = StringMap()
//        if (jsonEnumClass.isEnum == false) return result;
//        var fs = jsonEnumClass.declaredFields;
//        var remark: Field? = fs.firstOrNull { it.name == "remark" }
//
//
//        remark?.isAccessible = true;
//
//        fs.all {
//            if (it.name[0] == '$' && it.name.substring(1) == "VALUES") {
//                return@all true;
//            }
//            if (it.type != jsonEnumClass) return@all true;
//            var value = it.get(null);
//            result.put(it.name, remark?.get(value)?.toString() ?: it.name);
//            true;
//        }
//        return result;
//    }
}
