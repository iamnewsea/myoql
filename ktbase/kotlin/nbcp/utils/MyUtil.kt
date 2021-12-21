package nbcp.utils

import nbcp.comm.*
import java.lang.reflect.Field
import java.time.*
import java.util.*
import kotlin.reflect.KClass


/**
 * Created by nbcp on 2017/3/28.
 */

/**
 * 工具类。
 */

object MyUtil {
    //    val OneHourSeconds: Int = 3600000;
//    val OneDayMilliseconds: Int = 86400000;
    val OneDaySeconds: Int = 86400;
    val SystemTimeZoneOffsetTotalSeconds =
        ZoneId.systemDefault().rules.getStandardOffset(Date().toInstant()).totalSeconds  //系统时区相差的秒数

    @JvmStatic
    private val random = Random();

    //    /**
//     * 北京时间的今天凌晨。
//     */
    val today: LocalDate
        get() {
            return LocalDate.now();
        }


    /**
     * 通过 path 获取 value,每级返回的值必须是 Map<String,V> 否则返回 null
     * @param keys: 可以传多个key，也可以使用 . 分隔；如果查询数组，使用 products[],products[0], products.[] 或 products.[0] 或 "products","[]"
     */
    fun getValueByWbsPath(
        data: Any,
        vararg keys: String,
        ignoreCase: Boolean = false,
        fillMap: Boolean = false,
        fillLastArray: Boolean = false
    ): Any? {
        if (keys.any() == false) return null;

        var unwindKeys = keys
            .map { it.split('.') }
            .Unwind()
            .filter { it.HasValue }
            .toTypedArray();

        if (unwindKeys.size != keys.size) {
            return getValueByWbsPath(
                data,
                *unwindKeys,
                ignoreCase = ignoreCase,
                fillMap = fillMap,
                fillLastArray = fillLastArray
            );
        }

        var key = keys.first();
        var left_keys = keys.Slice(1);

        if (key.isEmpty()) {
            return null;
        }

        var isLastKey = left_keys.any() == false;

        if (key.endsWith("]")) {
            if (key != "[]" && key.endsWith("[]")) {
                return getValueByWbsPath(
                    data,
                    key.Slice(0, -2),
                    "[]",
                    ignoreCase = ignoreCase,
                    fillMap = fillMap,
                    fillLastArray = fillLastArray
                );
            }
            var start_index = key.lastIndexOf('[');
            if (start_index > 0) {
                return getValueByWbsPath(
                    data,
                    key.slice(0 until start_index),
                    key.Slice(start_index),
                    ignoreCase = ignoreCase,
                    fillMap = fillMap,
                    fillLastArray = fillLastArray
                )
            }
        }

        if (data is Map<*, *>) {
            var vbKeys = data.keys.filter { it.toString().compareTo(key, ignoreCase) == 0 }

            if (vbKeys.size > 1) {
                throw RuntimeException("找到多个 key: ${key}")
            } else if (vbKeys.size == 0) {
                if (isLastKey && fillLastArray) {
                    (data as MutableMap<String, Any?>).put(key, mutableListOf<Any?>());
                } else if (fillMap) {
                    if (left_keys.any() && left_keys.first().startsWith("[")) {
                        (data as MutableMap<String, Any?>).put(key, mutableListOf<Any?>());
                    } else {
                        (data as MutableMap<String, Any?>).put(key, JsonMap());
                    }
                } else {
                    return null;
                }
            } else {
                key = vbKeys.first().toString();
            }

            var v = data.get(key)!!

            if (left_keys.any() == false) return v;

            return getValueByWbsPath(
                v,
                *left_keys.toTypedArray(),
                ignoreCase = ignoreCase,
                fillMap = fillMap,
                fillLastArray = fillLastArray
            )
        } else if (key == "[]") {
            var data2: List<*>
            if (data is Array<*>) {
                data2 = data.filter { it != null }
            } else if (data is Collection<*>) {
                data2 = data.filter { it != null }
            } else {
                throw RuntimeException("数据类型不匹配,${keys} 中 ${key} 需要是数组类型")
            }

            if (left_keys.any() == false) return data2;

            return data2
                .map {
                    getValueByWbsPath(
                        it!!,
                        *left_keys.toTypedArray(),
                        ignoreCase = ignoreCase,
                        fillMap = fillMap,
                        fillLastArray = fillLastArray
                    )
                }
                .filter { it != null }

        } else if (key.startsWith("[") && key.endsWith("]")) {
            var index = key.substring(1, key.length - 1).AsInt(-1)
            if (index < 0) {
                throw RuntimeException("索引值错误:${key}")
            }

            var data2: Any?
            if (data is Array<*>) {
                data2 = data.get(index)
            } else if (data is Collection<*>) {
                //数组的数组很麻烦

                if (fillMap) {
                    for (i in data.size..index) {
                        (data as MutableList<Any?>).add(JsonMap())
                    }
                }

                data2 = data.elementAt(index)
            } else {
                throw RuntimeException("需要数组类型,但是实际类型是${data::class.java.name}, keys:${keys.joinToString(",")},key: ${key}")
            }

            if (data2 == null) {
                return null;
            }

            if (left_keys.any() == false) return data2;

            return getValueByWbsPath(
                data2,
                *left_keys.toTypedArray(),
                ignoreCase = ignoreCase,
                fillMap = fillMap,
                fillLastArray = fillLastArray
            )
        }

        //如果是对象

        var v = getPrivatePropertyValue(data, key)
        if (v == null) return null;
        if (left_keys.any() == false) {
            return v;
        }

        return getValueByWbsPath(
            v,
            *left_keys.toTypedArray(),
            ignoreCase = ignoreCase,
            fillMap = fillMap,
            fillLastArray = fillLastArray
        )
    }


    /**
     * 多层级设置值
     */
    fun setValueByWbsPath(data: Any, vararg keys: String, ignoreCase: Boolean = false, value: Any?): Boolean {
        if (keys.any() == false) return false;

        var unwindKeys = keys
            .map { it.split('.') }
            .Unwind()
            .map {
                var index = it.indexOf('[')
                if (index <= 0) {
                    return@map listOf(it)
                }
                return@map listOf(it.Slice(0, index), it.Slice(index))
            }
            .Unwind()
            .filter { it.HasValue }
            .toTypedArray();

        if (unwindKeys.size != keys.size) {
            return setValueByWbsPath(data, *unwindKeys, ignoreCase = ignoreCase, value = value);
        }

        var beforeKeys = keys.Slice(0, -1);
        var lastKey = keys.last()

        var objValue: Any? = data;

        if (beforeKeys.any()) {
            var fillLastArray = lastKey.startsWith("[") && lastKey.endsWith("]")
            objValue = getValueByWbsPath(
                data,
                *beforeKeys.toTypedArray(),
                ignoreCase = ignoreCase,
                fillMap = true,
                fillLastArray = fillLastArray
            );
        }

        if (objValue == null) {
            return false;
        }

        if (objValue is Map<*, *>) {
            if (objValue is MutableMap<*, *> == false) {
                throw RuntimeException("不是可修改的map")
            }

            var vbKeys = objValue.keys.filter { it.toString().compareTo(lastKey, ignoreCase) == 0 }

            if (vbKeys.size > 1) {
                throw RuntimeException("找到多个 key: ${lastKey}")
            } else if (vbKeys.any()) {
                lastKey = vbKeys.first().toString();
            }

            if (value == null) {
                (objValue as MutableMap<String, Any?>).remove(lastKey);
            } else {
                (objValue as MutableMap<String, Any?>).put(lastKey, value);
            }
            return true;
        } else if (objValue is Array<*>) {
            if (lastKey.startsWith("[") && lastKey.endsWith("]")) {
                var index = lastKey.substring(1, lastKey.length - 1).AsInt(-1)
                if (index < 0) {
                    throw RuntimeException("索引值错误:${lastKey},${index}")
                }

                (data as Array<Any?>).set(index, value);
                return true;
            }

            return false;
        } else if (objValue is Collection<*>) {
            if (lastKey.startsWith("[") && lastKey.endsWith("]")) {
                var index = lastKey.substring(1, lastKey.length - 1).AsInt(-1)
                if (index < 0) {
                    throw RuntimeException("索引值错误:${lastKey},${index}")
                }

                for (i in objValue.size..index) {
                    (objValue as MutableList<Any?>).add(JsonMap())
                }

                (objValue as MutableList<Any?>).set(index, value);
                return true;
            }

            return false;
        }

        //如果是对象
        return setPrivatePropertyValue(objValue, lastKey, ignoreCase = ignoreCase, value = value)
    }

    /**
     * 获取正在执行的方法信息
     */
    fun getCurrentMethodInfo(): StackTraceElement {
        return Thread.currentThread().getStackTrace()[2]!!
    }

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

//    /**
//     * 按大写字母拆分
//     */
//    fun splitWith(value: String,splitCallback:((Char)->Boolean)): List<String> {
//        var ret = mutableListOf<String>()
//
//        var prevIndex = 0;
//        for (index in 1 until value.length) {
//            var item = value[index];
//
//            if (item.isUpperCase()) {
//                ret.add(value.substring(prevIndex, index))
//                prevIndex = index;
//            }
//        }
//
//        ret.add(value.substring(prevIndex));
//
//        return ret.filter { it.HasValue };
//    }


    /**
     * https://www.w3school.com.cn/media/media_mimeref.asp
     */
    private val mimeLists = StringMap(
        "css" to "text/css",
        "htm" to "text/html",
        "html" to "text/html",
        "js" to "application/javascript",
        "xml" to "text/xml",
        "gif" to "image/gif",
        "jpg" to "image/jpeg",
        "jpeg" to "image/jpeg",
        "png" to "image/jpeg",
        "tiff" to "image/tiff",
        "json" to "application/json",
        "txt" to "text/plain",
        "mp3" to "audio/mpeg",
        "avi" to "video/x-msvideo",
        "mp4" to "video/mpeg4",
        "doc" to "application/msword",
        "docx" to "application/msword",
        "pdf" to "application/pdf",
        "xls" to "application/vnd.ms-excel",
        "xlsx" to "application/vnd.ms-excel",
        "ppt" to "application/vnd.ms-powerpoint",
        "exe" to "application/octet-stream",
        "zip" to "application/zip",
        "m3u" to "audio/x-mpegurl",
        "svg" to "image/svg+xml",
        "h" to "text/plain",
        "c" to "text/plain",
        "dll" to "application/x-msdownload"
    )

    fun getMimeType(extName: String): String {
        return mimeLists.getStringValue(extName.lowercase()).AsString("application/octet-stream")
    }


    /**
     * 最好传前8个字节，判断文件类型。
     */
    fun getFileTypeWithBom(byteArray8: ByteArray): String {
        //https://blog.csdn.net/hch15112345824/article/details/86640092
        //https://blog.csdn.net/gagapencil/article/details/40392363

        //小于4个字节，返回空。
        if (byteArray8.size < 4) {
            return "";
        }

        var value = byteArray8.ToHexLowerString()
        var map = mapOf(
            "4D546864" to "mid",
            "FFD8FF" to "jpg",
            "89504E47" to "png",
            "47494638" to "gif",
            "49492A00" to "tif",
            "424D" to "bmp",
            "41433130" to "dwg",
            "38425053" to "psd",
            "7B5C727466" to "rtf",
            "3C3F786D6C" to "xml",
            "68746D6C3E" to "html",
//                "44656C69766572792D646174653A" to "eml",
//                "CFAD12FEC5FD746F" to "dbx",
//                "2142444E" to "pst",
            "D0CF11E0" to "doc",
//                "5374616E64617264204A" to "mdb",
            "FF575043" to "wpd",
//                "252150532D41646F6265" to "ps",
            "255044462D312E" to "pdf",
            "AC9EBD8F" to "qdf",
            "E3828596" to "pwl",
            "504B0304" to "zip",
            "52617221" to "rar",
            "57415645" to "wav",
            "41564920" to "avi",
            "2E7261FD" to "ram",
            "2E524D46" to "rm",
            "000001BA" to "mpg",
            "000001B3" to "mpg",
            "6D6F6F76" to "mov",
            "3026B2758E66CF11" to "asf"
        );

        return map.filterKeys { value.startsWith(it) }.values.firstOrNull() ?: ""
    }

    fun isLocalIp(Ip: String): Boolean {
        return Ip.isEmpty() || Ip.startsWith("192.168.") || Ip.startsWith("10.") || Ip.startsWith("172.") || Ip.startsWith(
            "127."
        ) || Ip.startsWith("0.") || Ip.startsWith("0:")
    }

    /**
     * 生成大于等于0，小于指定最大值的随机数,即 [0,max)
     */
    fun getRandomWithMaxValue(max: Int): Int {
        return Math.abs(random.nextInt() % max);
    }

    /**
     * 生成指定长度的随机数
     */
    fun getRandomWithLength(length: Int): String {
        var ret = Math.abs(random.nextInt()).toString();
        while (true) {
            if (ret.length >= length) {
                break;
            }
            ret += Math.abs(random.nextInt()).toString();
        }
        ret = ret.slice(0..length - 1);
        return ret;
    }

    /**
     * 获取 Base64
     */
    fun getBase64(target: String): String {
        return Base64.getEncoder().encodeToString(target.toByteArray(const.utf8));
    }

    fun getBase64(bytes: ByteArray): String {
        return Base64.getEncoder().encodeToString(bytes);
    }

    fun getFromBase64(base64: String): ByteArray {
        return Base64.getDecoder().decode(base64);
    }

    fun getStringContentFromBase64(base64: String): String {
        return String(getFromBase64(base64), const.utf8)
    }
//
//    /**
//     * @param days: 从1开始的天数。（mongo dayOfYear 返回的天数）
//     */
//    fun getDate(year: Int, days: Int): Date {
//        val format = java.text.SimpleDateFormat("yyyy-MM-dd")
//
//        var currentYear0 = format.parse("${year}-01-01")
//        return Date(currentYear0.time + (days - 1) * OneDayMilliseconds);
//    }
//
//    //判断是否是北京时间的同一天。
//    fun isSameBjDay(date1: Date, date2: Date): Boolean {
//        return (date1.time + 8 * OneHourMilliseconds) / OneDayMilliseconds == (date2.time + 8 * OneHourMilliseconds) / OneDayMilliseconds;
//    }

//----------------------

    inline fun <reified T> checkEmpty(obj: T, mapDefine: HashMap<String, String>): String {
        var type = T::class.java;
        for (key in mapDefine.keys) {
            if (key.contains('.') == false) {
                var m = type.getMethod("get" + key[0].uppercaseChar() + key.substring(1));
                if (m == null) {
                    continue;
                }

                var v = m.invoke(obj);
                if (v == null || v.toString().isEmpty()) {
                    return mapDefine[key]!!
                }
                continue;
            }
        }
        return "";
    }


    fun getPrivatePropertyValue(entity: Any, type: Field): Any? {
        type.isAccessible = true;
        return type.get(entity);
    }

    /**
     * @param properties 多级属性 , 请使用 MyUtil.getValueByWbsPath
     */
    private fun getPrivatePropertyValue(entity: Any?, vararg properties: String, ignoreCase: Boolean = false): Any? {
        if (entity == null) return null;
        if (properties.any() == false) return null;

        var type = entity::class.java.FindField(properties.first(), ignoreCase);
        if (type != null) {
            var ret = type.get(entity);
            if (properties.size == 1) {
                return ret
            } else {
                var leftProperties = properties.slice(1 until properties.size).toTypedArray();
                return getPrivatePropertyValue(ret, *leftProperties)
            }
        }
        return null;
    }


    /**
     * 支持多层级设置属性值
     */
    fun setPrivatePropertyValue(
        entity: Any?,
        vararg properties: String,
        ignoreCase: Boolean = false,
        value: Any?
    ): Boolean {
        if (entity == null) return false;
        if (properties.any() == false) return false;

        var type = entity::class.java.FindField(properties.first(), ignoreCase);
        if (type != null) {
            var ret = type.get(entity);
            if (properties.size == 1) {
                type.set(entity, value);
                return true
            } else {
                var leftProperties = properties.slice(1 until properties.size).toTypedArray();
                return setPrivatePropertyValue(ret, *leftProperties, ignoreCase = ignoreCase, value = value)
            }
        }
        return false;
    }

    fun setPrivatePropertyValue(entity: Any, property: String, value: Any?): Boolean {
        return setPrivatePropertyValue(entity, *arrayOf(property), ignoreCase = false, value = value);
    }

//    fun setPropertyValue(entity: Any, property: String, value: Any?) {
//        var type = entity::class.java.AllFields.firstOrNull { it.name == property };
//        if (type == null) {
//            return;
//        }
////        type.isAccessible = true;
//        type.set(entity, value)
//    }

    /**
     * 通过反射把 源对象值传输到目标对象
     */
    fun transportValueWithReflect(src: Any, target: Any) {
        var srcFields = src::class.java.AllFields;
        target::class.java.AllFields.forEach { targetField ->
            var srcField = srcFields.firstOrNull { it.name == targetField.name }
            if (srcField == null) {
                return@forEach
            }
            targetField.set(target, srcField.get(src))
        }
    }

    fun <T : Any> getSimpleClassDefaultValue(clazz: KClass<T>): Any? {
        return getSimpleClassDefaultValue(clazz.java)
    }

    //简单类型，请参考 Class.IsSimpleType
    fun <T> getSimpleClassDefaultValue(clazz: Class<T>): Any? {

//        var className = clazz.name;
        if (clazz == Boolean::class.java || clazz == java.lang.Boolean::class.java) {
            return false;
        }
        if (clazz == java.lang.Character::class.java || clazz == Char::class.java) {
            return '\u0000';
        }

        if (clazz == Byte::class.java || clazz == java.lang.Byte::class.java) {
            return 0;
        }
        if (clazz == Short::class.java || clazz == java.lang.Short::class.java) {
            return 0;
        }
        if (clazz == java.lang.Integer::class.java || clazz == Int::class.java) {
            return 0;
        }
        if (clazz == Long::class.java || clazz == java.lang.Long::class.java) {
            return 0L;
        }
        if (clazz == Float::class.java || clazz == java.lang.Float::class.java) {
            return 0F;
        }
        if (clazz == Double::class.java || clazz == java.lang.Double::class.java) {
            return 0.0;
        }

        //不应该执行这句.
        if (clazz.isPrimitive) {
            return null;
        } else if (clazz.isEnum) {
            return clazz.declaredFields.first().get(null);
        } else if (clazz == String::class.java) {
            return "";
        } else if (clazz == java.time.LocalDate::class.java) {
            return LocalDate.MIN
        } else if (clazz == java.time.LocalTime::class.java) {
            return LocalTime.MIN
        } else if (clazz == java.time.LocalDateTime::class.java) {
            return LocalDateTime.MIN
        } else if (clazz == java.util.Date::class.java) {
            return Date(0)
        }
//        else if (clazz == org.bson.types.ObjectId::class.java) {
//            return ObjectId(0, 0, 0, 0)
//        }

        return null;
    }

    fun allCharIsUpperCase(value: String): Boolean {
        return value.all { it.isUpperCase() }
    }

    fun allCharIsLowerCase(value: String): Boolean {
        return value.all { it.isLowerCase() }
    }

    /**
     * 是否全大小，或全小写。 有任意字符则返回false
     */
    fun allCharIsSameCase(value: String): Boolean {
        if (value.length <= 1) return true;
        if (value[0].isUpperCase()) return allCharIsUpperCase(value);
        if (value[0].isLowerCase()) return allCharIsLowerCase(value);
        return false;
    }

    /**
     * 判断是否存在不一样的字符，忽略特殊字符
     */
    fun noAnyOtherCase(value: String): Boolean {
        return allCharIsSameCase(value.replace(Regex("[\\W_]"), ""))
    }

    /**
     * 分隔为各个部分
     */
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

                    if (MyUtil.allCharIsUpperCase(prevItem) && item[0].isUpperCase()) {
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
    fun getBigCamelCase(value: String): String {
        return splitWordParts(value).map { it[0].uppercaseChar() + it.substring(1).lowercase() }.joinToString("")
    }

    /**
     * 小驼峰
     */
    fun getSmallCamelCase(value: String): String {
        var ret = getBigCamelCase(value);
        if (ret.isEmpty()) return "";
        return ret[0].lowercase() + ret.substring(1)
    }

    /**
     * 短横线格式，全小写
     */
    fun getKebabCase(value: String): String {
        return splitWordParts(value).map { it.lowercase() }.joinToString("-")
    }

    @JvmOverloads
    fun trimStart(value: String, trimPart: String, ignoreCase: Boolean = false): String {
        if (value.startsWith(trimPart, ignoreCase) == false) {
            return value;
        }
        return trimStart(value.substring(trimPart.length), trimPart, ignoreCase)
    }

    @JvmOverloads
    fun trimEnd(value: String, trimPart: String, ignoreCase: Boolean = false): String {
        if (value.endsWith(trimPart, ignoreCase) == false) {
            return value;
        }
        return trimEnd(value.substring(0, value.length - trimPart.length), trimPart, ignoreCase)
    }

    @JvmOverloads
    fun trim(value: String, trimPart: String, ignoreCase: Boolean = false): String {
        return trimEnd(trimStart(value, trimPart, ignoreCase), trimPart, ignoreCase)
    }

    /**
     * 格式化模板
     */
    fun formatTemplateJson(
        /**
         * 如 dbr.${group|w}
         */
        text: String,
        /**
         * 如: {group:"abc"}
         */
        json: StringMap,
        /**
         * 不是默认函数的时候,调用 funcCallback 自定义处理.
         * 第一个参数是key, 第二个是value, 第三个参数是函数名,第四个参数是函数参数 , 返回新值
         * 如果模板使用了函数,而没有传递,抛出异常.
         * 如: ${id|type} ,type(id) 不是默认定义,需要通过 funcCallback 传
         */
        funcCallback: ((String, String?, String, String) -> String?)? = null,
        style: String = "\${}"
    ): String {

        var map: StringKeyMap<((String) -> String)> = StringKeyMap()
        map.put("-", { getKebabCase(it) })
        map.put("W", { getBigCamelCase(it) })
        map.put("w", { getSmallCamelCase(it) })
        map.put("U", { it.uppercase() })
        map.put("u", { it.lowercase() })

        var map2: StringKeyMap<((String).(String) -> String)> = StringKeyMap()
        map2.put("trim", { trim(this, it) })


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
                        if (value != null && map2.containsKey(funcName)) {
                            var funcBody = map2.get(funcName)!!
                            result = funcBody.invoke(value, paramValue)
                        } else if (funcCallback != null) {
                            result = funcCallback.invoke(key, value, funcName, paramValue)
                        } else {
                            throw RuntimeException("找不到 ${funcName}")
                        }
                    } else if (params.size == 0) {
                        if (value != null && map.containsKey(funcName)) {
                            val funcBody = map.get(funcName)!!
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

