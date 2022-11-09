package nbcp.myoql.tool

import nbcp.base.extend.*;
import nbcp.base.utils.*;
import nbcp.base.utils.MyUtil

/**
 * 枚举Js的生成器
 */
object Enum4JsGenerator {

    /**
     * 入口
     */
    @JvmStatic
    fun work(basePackage: String): List<String> {
        var ret = mutableListOf<String>()
        val fileList = ClassUtil.getClassesWithBaseType(basePackage, Enum::class.java)

        fileList.forEach {
            ret.add(work(it))
        }
        return ret;
    }

    @JvmStatic
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
