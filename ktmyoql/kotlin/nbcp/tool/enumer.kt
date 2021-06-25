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
        val fileList = ClassUtil.getClassesWithBaseType(basePackage, Enum::class.java)

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
}
