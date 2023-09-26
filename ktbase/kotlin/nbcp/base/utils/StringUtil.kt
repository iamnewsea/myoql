package nbcp.base.utils

import nbcp.base.comm.StringKeyMap
import nbcp.base.comm.StringMap
import nbcp.base.extend.*

object StringUtil {


    @JvmStatic
    fun getCenterEachLine(lines: List<String>): List<String> {
        var selector: (Char) -> Int = {
            if (it.code < 256) 1 else 2
        }

        var map = lines.map { it to it.sumOf(selector) }

        var max = map.map { it.second }.maxOrNull() ?: 0
        if (max % 2 == 1) {
            max++;
        }

        return map.map {
            if (it.second == 0) return@map it.first

            var harf = ((max - it.second) / 2).AsInt()
            var part = ' '.NewString(harf)
            return@map part + it.first
        }
    }


    /**
     * @sample  \\344\\270\\212\\347\\272\\277  --> 上线
     */
    @JvmStatic
    fun decodeStringFromFanOctalCode(fanHex: String): String {
        if (fanHex.startsWith("\\") == false) {
            throw RuntimeException("必须以反斜线开头")
        }

        return fanHex.substring(1)
                .split("\\")
                .map { it.toInt(8).toByte() }
                .toByteArray()
                .toUtf8String()
    }

    @JvmStatic
    fun allCharIsUpperCase(value: String): Boolean {
        return value.all { it.isUpperCase() }
    }

    @JvmStatic
    fun allCharIsLowerCase(value: String): Boolean {
        return value.all { it.isLowerCase() }
    }

    /**
     * 是否全大小，或全小写。 有任意字符则返回false
     */
    @JvmStatic
    fun allCharIsSameCase(value: String): Boolean {
        if (value.length <= 1) return true;
        if (value[0].isUpperCase()) return allCharIsUpperCase(value);
        if (value[0].isLowerCase()) return allCharIsLowerCase(value);
        return false;
    }

    /**
     * 判断是否存在不一样的字符，忽略特殊字符
     */
    @JvmStatic
    fun noAnyOtherCase(value: String): Boolean {
        return allCharIsSameCase(value.replace(Regex("[\\W_]"), ""))
    }

    /**
     * 分隔为各个部分
     */
    @JvmStatic
    fun splitWordParts(value: String): List<String> {
        var ret = value.split(Regex("""[\W_]+""")).map {
            if (allCharIsSameCase(it)) {
                return@map listOf(it);
            } else {
                //连续大写，按一部分处理
                var list = it.split(Regex("(?=[A-Z])")).toMutableList();
                if (list.any() == false) return@map listOf()
                //合并连续大写
                var prevItem = list[0];
                for (i in 1 until list.size) {
                    var item = list[i];

                    if (item.HasValue && item[0].isUpperCase() && allCharIsUpperCase(prevItem)) {
                        prevItem = prevItem + item
                        list.set(i, prevItem);
                        list.set(i - 1, "");
                    }
                }

                return@map list.filter { it.HasValue }
            }
        }
                .Unwind()
                .filter { it.HasValue }

        return ret;
    }

    /**
     * 大驼峰 ,仅保留字母，数字
     */
    @JvmStatic
    fun getBigCamelCase(value: String): String {
        return splitWordParts(value).map { it[0].uppercaseChar() + it.substring(1).lowercase() }.joinToString("")
    }

    /**
     * 小驼峰
     */
    @JvmStatic
    fun getSmallCamelCase(value: String): String {
        var ret = getBigCamelCase(value);
        if (ret.isEmpty()) return "";
        return ret[0].lowercase() + ret.substring(1)
    }

    /**
     * 是否是短横线格式
     */
    @JvmStatic
    fun isKebabCase(value: String): Boolean {
        return value.split("-").any { allCharIsLowerCase(it) }
    }

    /**
     * 短横线格式，全小写
     */
    @JvmStatic
    fun getKebabCase(value: String): String {
        return splitWordParts(value).map { it.lowercase() }.joinToString("-")
    }

    /**
     * 下划线格式，全小写
     */
    @JvmStatic
    fun getUnderlineCase(value: String): String {
        return splitWordParts(value).map { it.lowercase() }.joinToString("_")
    }

    @JvmOverloads
    @JvmStatic
    fun trimStart(value: String, trimPart: String, ignoreCase: Boolean = false): String {
        if (value.startsWith(trimPart, ignoreCase) == false) {
            return value;
        }
        return trimStart(value.substring(trimPart.length), trimPart, ignoreCase)
    }

    @JvmOverloads
    @JvmStatic
    fun trimEnd(value: String, trimPart: String, ignoreCase: Boolean = false): String {
        if (value.endsWith(trimPart, ignoreCase) == false) {
            return value;
        }
        return trimEnd(value.substring(0, value.length - trimPart.length), trimPart, ignoreCase)
    }

    @JvmOverloads
    @JvmStatic
    fun trim(value: String, trimPart: String, ignoreCase: Boolean = false): String {
        return trimEnd(trimStart(value, trimPart, ignoreCase), trimPart, ignoreCase)
    }

    /**
     * 使用变量格式化字符串
     *
     * @param text 模板，其中变量内容可以定 ${varName|trim|bc}
     * @param funcCallback
     * 不是默认函数的时候,调用 funcCallback 自定义处理.
     * 第一个参数是key, 第二个是value, 第三个参数是函数名,第四个参数是函数参数 , 返回新值
     * 如果模板使用了函数,而没有传递,抛出异常.
     * 如: ${id|type} ,type(id) 不是默认定义,需要通过 funcCallback 传
     */
    @JvmStatic
    @JvmOverloads
    fun formatTemplateJson(
            /**
             * 如 dbr.${group|trim:.|bc}
             */
            text: String,
            /**
             * 如: {group:"abc"}
             */
            json: StringMap,
            funcCallback: ((String, String?, String, String) -> String?)? = null,
            style: String = "\${}"
    ): String {

        var styleMap: StringKeyMap<((String) -> String)> = StringKeyMap()
        styleMap.put("-", { getKebabCase(it) })
        styleMap.put("_", { getUnderlineCase(it) })
        styleMap.put("bc", { getBigCamelCase(it) })
        styleMap.put("sc", { getSmallCamelCase(it) })
        styleMap.put("u", { it.uppercase() })
        styleMap.put("l", { it.lowercase() })
        styleMap.put("trim", { it.trim() })

        var fuctionMap: StringKeyMap<((String).(String) -> String)> = StringKeyMap()
        fuctionMap.put("trim", { trim(this, it) })


        return text.formatWithJson(
                json, style,
                { key ->
                    key.split("|").first()
                },
                { fullKey, value ->
                    // fullKey 即 ${fullKey} == group|w
                    // 如果 json中定义了值,使用json的.如: json == {"group|w": "大写第二个字母值"}
                    // value是group的原始值 == "wx"
                    var sects = fullKey.split("|")
                    if (sects.size <= 1) {
                        return@formatWithJson value;
                    }

                    // key == group
                    var key = sects.first();
                    var result: String? = value
                    sects.Skip(1).forEach { funcString ->
                        //如：   substring:2,3
                        var funcContent = funcString.split(":")
                        var funcName = funcContent.first();
                        var params = listOf<String>()
                        if (funcContent.size > 2) {
                            throw RuntimeException("表达式中多个冒号非法：${funcString}")
                        } else if (funcContent.size == 2) {
                            // 如：   substring:2,3 中的 2,2 参数部分
                            params = funcContent[1].split(",")
                        }


                        if (params.size == 1) {
                            var param = params[0];
                            var paramValue = param;
                            if (param.startsWith("'") && param.endsWith("'")) {
                                paramValue = param.substring(1, param.length - 1);
                            } else if (param.IsNumberic()) {
                                paramValue = param;
                            } else {
                                paramValue = json.get(param).AsString()
                            }

                            //如果定义了默认的funcName
                            if (value != null && fuctionMap.containsKey(funcName)) {
                                var funcBody = fuctionMap.get(funcName)!!
                                result = funcBody.invoke(value, paramValue)
                            } else if (funcCallback != null) {
                                result = funcCallback.invoke(key, value, funcName, paramValue)
                            } else {
                                throw RuntimeException("找不到 ${funcName}")
                            }
                        } else if (params.size == 0) {
                            if (value != null && styleMap.containsKey(funcName)) {
                                val funcBody = styleMap.get(funcName)!!
                                result = funcBody.invoke(value)
                            } else if (funcCallback != null) {
                                result = funcCallback.invoke(key, value, funcName, "")
                            } else {
                                throw RuntimeException("找不到 ${funcName}")
                            }

                        }

                        return@forEach
                    }

                    if (result == null) {
                        throw RuntimeException("无法处理 ${fullKey}")
                    }

                    return@formatWithJson result!!
                });
    }
}