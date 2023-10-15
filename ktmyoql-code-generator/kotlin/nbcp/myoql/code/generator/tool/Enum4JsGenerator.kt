package nbcp.myoql.code.generator.tool

import nbcp.base.extend.GetEnumJsonValueValue
import nbcp.base.extend.GetEnumList
import nbcp.base.extend.GetEnumStringField
import nbcp.base.utils.ClassUtil
import nbcp.base.utils.ReflectUtil

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
        val fileList = ClassUtil.findClasses(basePackage, false, Enum::class.java)

        fileList.forEach {
            ret.add(work(it as Class<out Enum<*>>))
        }
        return ret;
    }

    @JvmStatic
    fun <T : Enum<T>> work(jsonEnumClass: Class<T>): String {
        if (jsonEnumClass.isEnum == false) {
            return "";
        }
        var jsonList = jsonEnumClass.GetEnumList()

        var ret = "";
        if (jsonList.size > 0) {
            var strField = jsonEnumClass.GetEnumStringField();

            ret = """jv.defEnum("${jsonEnumClass.simpleName}",{""" +
                    jsonList.map {
                        it.GetEnumJsonValueValue() + ":\"" +
                                (if (strField != null) ReflectUtil.getPrivatePropertyValue(
                                    it,
                                    strField
                                ) else it.toString()) + "\""
                    }.joinToString(",") + "});"

            println(ret);
        }

        return ret;
    }
}
